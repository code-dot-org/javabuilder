package org.code.javabuilder;

import static org.code.javabuilder.DashboardConstants.DASHBOARD_LOCALHOST_DOMAIN;
import static org.code.javabuilder.LambdaErrorCodes.TEMP_DIRECTORY_CLEANUP_ERROR_CODE;
import static org.code.protocol.InternalExceptionKey.INTERNAL_EXCEPTION;
import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.DeleteConnectionRequest;
import com.amazonaws.services.apigatewaymanagementapi.model.GoneException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.code.protocol.*;
import org.code.validation.support.UserTestOutputAdapter;
import org.json.JSONObject;

/**
 * This is the entry point for the lambda function. This should be thought of as similar to a main
 * function, but with a specific input contract.
 * https://github.com/awsdocs/aws-lambda-developer-guide/tree/main/sample-apps/blank-java
 */
public class LambdaRequestHandler implements RequestHandler<Map<String, String>, String> {
  private static final Instant COLD_BOOT_START = Clock.systemUTC().instant();
  private final Instant COLD_BOOT_END;
  private static boolean coldBoot = true;
  private static final int CHECK_THREAD_INTERVAL_MS = 500;
  private static final int TIMEOUT_WARNING_MS = 20000;
  private static final int TIMEOUT_CLEANUP_BUFFER_MS = 5000;
  private static final String LAMBDA_ID = UUID.randomUUID().toString();
  // This is an error code we made up to signal that available disk space is less than 50%.
  // It may be used in tracking on Cloud Watch.
  private static final int LOW_DISK_SPACE_ERROR_CODE = 50;
  private static final String CONTENT_BUCKET_NAME = System.getenv("CONTENT_BUCKET_NAME");
  private static final String CONTENT_BUCKET_URL = System.getenv("CONTENT_BUCKET_URL");
  private static final String API_ENDPOINT = System.getenv("API_ENDPOINT");

  // Creating these clients here rather than in the request handler method allows us to use
  // provisioned concurrency to decrease cold boot time by 3-10 seconds, depending on the lambda
  private static final AmazonApiGatewayManagementApi API_CLIENT =
      AmazonApiGatewayManagementApiClientBuilder.standard()
          .withEndpointConfiguration(
              new AwsClientBuilder.EndpointConfiguration(API_ENDPOINT, "us-east-1"))
          .build();
  private static final AmazonSQS SQS_CLIENT = AmazonSQSClientBuilder.defaultClient();
  private static final AmazonS3 S3_CLIENT = AmazonS3ClientBuilder.standard().build();

  public LambdaRequestHandler() {
    // create CachedResources once for the entire container.
    // This will only be called once in the initial creation of the lambda instance.
    // Documentation: https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html
    CachedResources.create();
    COLD_BOOT_END = Clock.systemUTC().instant();
  }

  /**
   * This is the implementation of the long-running-lambda where user code will be compiled and
   * executed.
   *
   * @param lambdaInput A map of the json input to this lambda function. Input is formatted like
   *     this: { "queueUrl": <session-specific-url>, "queueName": <session-specific-string>
   *     "connectionId": <session-specific-string>, "channelId": <project-specific-string> }
   * @param context This is the context in which the lambda was invoked.
   * @return A string. Right now, we aren't using this, so anything can be returned.
   */
  @Override
  public String handleRequest(Map<String, String> lambdaInput, Context context) {
    final Instant instanceStart = Clock.systemUTC().instant();
    // The lambda handler should have minimal application logic:
    // https://docs.aws.amazon.com/lambda/latest/dg/best-practices.html
    final String connectionId = lambdaInput.get("connectionId");
    final String queueUrl = lambdaInput.get("queueUrl");
    final String queueName = lambdaInput.get("queueName");
    final String levelId = lambdaInput.get("levelId");
    final String channelId =
        lambdaInput.get("channelId") == null ? "noneProvided" : lambdaInput.get("channelId");
    final ExecutionType executionType = ExecutionType.valueOf(lambdaInput.get("executionType"));
    final String dashboardHostname = lambdaInput.get("iss");
    final JSONObject options = new JSONObject(lambdaInput.get("options"));
    final String javabuilderSessionId = lambdaInput.get("javabuilderSessionId");
    final List<String> compileList = JSONUtils.listFromJSONObjectMember(options, "compileList");

    Logger logger = Logger.getLogger(MAIN_LOGGER);
    logger.addHandler(
        new LambdaLogHandler(
            context.getLogger(),
            javabuilderSessionId,
            connectionId,
            levelId,
            LambdaRequestHandler.LAMBDA_ID,
            channelId));
    // turn off the default console logger
    logger.setUseParentHandlers(false);
    Properties.setConnectionId(connectionId);

    MetricClient metricClient = new AWSMetricClient(context.getFunctionName());
    MetricClientManager.create(metricClient);

    PerformanceTracker.resetTracker();
    if (coldBoot) {
      PerformanceTracker.getInstance().trackColdBoot(COLD_BOOT_START, COLD_BOOT_END, instanceStart);
      coldBoot = false;
    } else {
      PerformanceTracker.getInstance().trackInstanceStart(instanceStart);
    }

    // Dashboard assets are only accessible if the dashboard domain is not localhost
    Properties.setCanAccessDashboardAssets(!dashboardHostname.equals(DASHBOARD_LOCALHOST_DOMAIN));

    // Create user-program output handlers
    final AWSOutputAdapter awsOutputAdapter = new AWSOutputAdapter(connectionId, API_CLIENT);

    // Create user input handlers
    final AWSInputAdapter inputAdapter = new AWSInputAdapter(SQS_CLIENT, queueUrl, queueName);
    final AWSTempDirectoryManager tempDirectoryManager = new AWSTempDirectoryManager();
    final LifecycleNotifier lifecycleNotifier = new LifecycleNotifier();
    OutputAdapter outputAdapter = awsOutputAdapter;
    if (executionType == ExecutionType.TEST) {
      outputAdapter = new UserTestOutputAdapter(awsOutputAdapter);
    }

    final AWSContentManager contentManager;
    try {
      contentManager =
          new AWSContentManager(
              S3_CLIENT, CONTENT_BUCKET_NAME, javabuilderSessionId, CONTENT_BUCKET_URL, context);
    } catch (InternalServerException e) {
      return onInitializationError("error loading data", e, outputAdapter, connectionId);
    }

    // manually set font configuration file since there is no font configuration on a lambda.
    java.util.Properties props = System.getProperties();
    // /opt is the folder all layer files go into.
    props.put("sun.awt.fontconfig", "/opt/fontconfig.properties");

    try {
      // Log disk space before clearing the directory
      LoggerUtils.sendDiskSpaceReport();

      tempDirectoryManager.cleanUpTempDirectory(null);
    } catch (IOException e) {
      // Wrap this in our error type so we can log it and tell the user.
      InternalServerException error = new InternalServerException(INTERNAL_EXCEPTION, e);
      onInitializationError("error clearing tmpdir", error, outputAdapter, connectionId);
      // If there was an issue clearing the temp directory, this may be because too many files are
      // open. Force the JVM to quit in order to release the resources for the next use of the
      // container.
      System.exit(TEMP_DIRECTORY_CLEANUP_ERROR_CODE);
    }

    final CodeExecutionManager codeExecutionManager =
        new CodeExecutionManager(
            contentManager.getProjectFileLoader(),
            inputAdapter,
            outputAdapter,
            executionType,
            compileList,
            tempDirectoryManager,
            contentManager,
            lifecycleNotifier,
            new AWSSystemExitHelper(connectionId, API_CLIENT));

    final Thread timeoutNotifierThread =
        createTimeoutThread(context, outputAdapter, codeExecutionManager, connectionId, API_CLIENT);
    timeoutNotifierThread.start();

    try {
      // start code build and block until completed
      codeExecutionManager.execute();
    } catch (Throwable e) {
      // All errors should be caught, but if for any reason we encounter an error here, make sure we
      // catch it, log, and always clean up resources
      LoggerUtils.logSevereException(e);
    } finally {
      // Stop timeout listener and clean up
      timeoutNotifierThread.interrupt();
      PerformanceTracker.getInstance().trackInstanceEnd();
      PerformanceTracker.getInstance().logPerformance();
      cleanUpResources(connectionId, API_CLIENT);
      File f = Paths.get(System.getProperty("java.io.tmpdir")).toFile();
      if ((double) f.getUsableSpace() / f.getTotalSpace() < 0.5) {
        // The current project holds a lock on too many resources. Force the JVM to quit in
        // order to release the resources for the next use of the container.
        System.exit(LOW_DISK_SPACE_ERROR_CODE);
      }
    }

    return "done";
  }

  /** Handles logging and cleanup in the case of an initialization error. */
  private String onInitializationError(
      String errorMessage,
      InternalServerException error,
      OutputAdapter outputAdapter,
      String connectionId) {
    // Log the error
    LoggerUtils.logSevereError(error);

    // This affected the user. Let's tell them about it.
    outputAdapter.sendMessage(error.getExceptionMessage());
    PerformanceTracker.getInstance().trackInstanceEnd();
    PerformanceTracker.getInstance().logPerformance();
    cleanUpResources(connectionId, API_CLIENT);
    return errorMessage;
  }

  private Thread createTimeoutThread(
      Context context,
      OutputAdapter outputAdapter,
      CodeExecutionManager codeExecutionManager,
      String connectionId,
      AmazonApiGatewayManagementApi api) {
    return new Thread(
        () -> {
          boolean timeoutWarningSent = false;

          while (!Thread.currentThread().isInterrupted()) {
            try {
              Thread.sleep(CHECK_THREAD_INTERVAL_MS);
              if ((context.getRemainingTimeInMillis() < TIMEOUT_WARNING_MS)
                  && !timeoutWarningSent) {
                outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.TIMEOUT_WARNING));
                timeoutWarningSent = true;
              }

              if (context.getRemainingTimeInMillis() < TIMEOUT_CLEANUP_BUFFER_MS) {
                outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.TIMEOUT));
                // Tell the execution manager to clean up early
                codeExecutionManager.requestEarlyExit();
                // Clean up AWS resources
                cleanUpResources(connectionId, api);
                break;
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
        });
  }

  /**
   * Note: This can sometimes be called twice when a user's project times out. Make sure anything
   * added here can be run more than once without negative effect.
   */
  private void cleanUpResources(String connectionId, AmazonApiGatewayManagementApi api) {
    // delete the metric client manager if it exists
    MetricClientManager.destroy();
    final DeleteConnectionRequest deleteConnectionRequest =
        new DeleteConnectionRequest().withConnectionId(connectionId);
    // Deleting the API Gateway connection should always be the last thing executed because the
    // delete action cleans up the AWS resources associated with this lambda
    try {
      api.deleteConnection(deleteConnectionRequest);
    } catch (GoneException e) {
      // if the connection is already gone, we don't need to delete the connection.
    }
    // clean up log handler to avoid duplicate logs in future runs.
    Handler[] allHandlers = Logger.getLogger(MAIN_LOGGER).getHandlers();
    for (int i = 0; i < allHandlers.length; i++) {
      Logger.getLogger(MAIN_LOGGER).removeHandler(allHandlers[i]);
    }
  }
}
