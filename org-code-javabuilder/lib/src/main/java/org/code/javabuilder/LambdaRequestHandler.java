package org.code.javabuilder;

import static org.code.javabuilder.InternalFacingExceptionTypes.INVALID_INPUT;
import static org.code.protocol.InternalExceptionKey.*;
import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.DeleteConnectionRequest;
import com.amazonaws.services.apigatewaymanagementapi.model.GetConnectionRequest;
import com.amazonaws.services.apigatewaymanagementapi.model.GoneException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
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
import org.code.javabuilder.util.LambdaUtils;
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
  private static final String CONTENT_BUCKET_NAME = System.getenv("CONTENT_BUCKET_NAME");
  private static final String CONTENT_BUCKET_URL = System.getenv("CONTENT_BUCKET_URL");
  private static final String API_ENDPOINT = System.getenv("API_ENDPOINT");
  private static final String UNHEALTHY_CONTAINERS_TABLE_NAME =
      System.getenv("UNHEALTHY_CONTAINERS_TABLE_NAME");

  // Creating these clients here rather than in the request handler method allows us to use
  // provisioned concurrency to decrease cold boot time by 3-10 seconds, depending on the lambda
  private static final AmazonSQS SQS_CLIENT = AmazonSQSClientBuilder.defaultClient();
  private static final AmazonS3 S3_CLIENT = AmazonS3ClientBuilder.standard().build();
  private static final AmazonDynamoDB DYNAMO_DB_CLIENT =
      AmazonDynamoDBClientBuilder.defaultClient();

  private final UnhealthyContainerChecker unhealthyContainerChecker;

  // Controls whether the current invocation session has been initialized. This should be reset on
  // every invocation.
  private boolean isSessionInitialized = false;
  // API Gateway Client. We create this in the constructor so we can recreate it if it goes away for
  // some reason.
  private AmazonApiGatewayManagementApi apiClient;

  public LambdaRequestHandler() {
    // create CachedResources once for the entire container.
    // This will only be called once in the initial creation of the lambda instance.
    // Documentation: https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html
    CachedResources.create();
    COLD_BOOT_END = Clock.systemUTC().instant();
    this.apiClient =
        AmazonApiGatewayManagementApiClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(API_ENDPOINT, "us-east-1"))
            // .withMonitoringListener(new JavabuilderMonitoringListener())
            .build();

    this.unhealthyContainerChecker =
        new UnhealthyContainerChecker(DYNAMO_DB_CLIENT, UNHEALTHY_CONTAINERS_TABLE_NAME);
  }

  /**
   * This is the implementation of the long-running-lambda where user code will be compiled and
   * executed. The handler performs the following actions for each invocation:
   *
   * <p>1. Initialize the current session (set global properties, setup static objects, etc)
   *
   * <p>2.Create the OutputAdapter to communicate with the user
   *
   * <p>3. Clear the temp directory on the current container
   *
   * <p>4. Setup the code execution environment, and execute code
   *
   * <p>5. Handle any exceptions/errors if they are thrown
   *
   * <p>6. Shut down the current session (shut down the execution environment, clean up AWS
   * resources, etc).
   *
   * @param lambdaInput A map of the json input to this lambda function. Input is formatted like
   *     this: { "queueUrl": <session-specific-url>, "queueName": <session-specific-string>
   *     "connectionId": <session-specific-string>, "channelId": <project-specific-string> }
   * @param context This is the context in which the lambda was invoked.
   * @return A string. Right now, we aren't using this, so anything can be returned.
   */
  @Override
  public String handleRequest(Map<String, String> lambdaInput, Context context) {
    this.isSessionInitialized = false;
    JavabuilderContext.getInstance().destroyAndReset();
    this.trackStartupPerformance();

    // TODO: Because we reference the logger object throughout the codebase via
    // Logger.getLogger(MAIN_LOGGER), we need to set it up in the same scope as code execution to
    // prevent the instance from being garbage collected. Instead, we should store a global
    // reference to this logger object and access that directly rather than via
    // Logger.getLogger(MAIN_LOGGER)
    final String connectionId = lambdaInput.get("connectionId");
    final String levelId = lambdaInput.get("levelId");
    final String channelId =
        lambdaInput.get("channelId") == null ? "noneProvided" : lambdaInput.get("channelId");
    final String javabuilderSessionId = lambdaInput.get("javabuilderSessionId");
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

    this.initialize(lambdaInput, connectionId, context);

    // Try to construct the output adapter as early as possible, so we can notify the user if
    // something goes wrong. If for some reason we cannot construct the output adapter, our only
    // option is to log and exit.
    final OutputAdapter outputAdapter;
    try {
      outputAdapter = this.createOutputAdapter(lambdaInput);
    } catch (InternalFacingException e) {
      LoggerUtils.logSevereException(e);
      return "error";
    }

    final ExceptionHandler exceptionHandler =
        new ExceptionHandler(outputAdapter, new AWSSystemExitHelper(connectionId, this.apiClient));
    final TempDirectoryManager tempDirectoryManager = new AWSTempDirectoryManager();

    CodeExecutionManager codeExecutionManager = null;
    Thread timeoutNotifierThread = null;

    try {
      this.clearTempDirectory(tempDirectoryManager);

      codeExecutionManager =
          this.createExecutionManager(
              lambdaInput, context, connectionId, outputAdapter, tempDirectoryManager);

      // Create and start thread that that will notify us if we're nearing the timeout limit
      timeoutNotifierThread =
          this.createTimeoutThread(
              context, outputAdapter, codeExecutionManager, connectionId, this.apiClient);
      timeoutNotifierThread.start();

      // Initialize and start code execution
      codeExecutionManager.execute();
    } catch (Throwable e) {
      // Catch and handle all exceptions
      exceptionHandler.handle(e);
    } finally {
      if (timeoutNotifierThread != null) {
        timeoutNotifierThread.interrupt();
      }
      this.shutDown(codeExecutionManager, connectionId, this.apiClient);
    }

    return "done";
  }

  /**
   * Sets up the lambda environment for the current invocation by setting global properties and
   * creating global objects
   */
  private void initialize(Map<String, String> lambdaInput, String connectionId, Context context) {
    final boolean canAccessDashboardAssets =
        Boolean.parseBoolean(lambdaInput.get("canAccessDashboardAssets"));

    Properties.setConnectionId(connectionId);

    AWSMetricClient metricClient = new AWSMetricClient(context.getFunctionName());
    JavabuilderContext.getInstance().register(MetricClient.class, metricClient);

    // Dashboard assets are only accessible if the dashboard domain is not localhost
    Properties.setCanAccessDashboardAssets(canAccessDashboardAssets);
    // manually set font configuration file since there is no font configuration on a lambda.
    java.util.Properties props = System.getProperties();
    // /opt is the folder all layer files go into.
    props.put("sun.awt.fontconfig", "/opt/fontconfig.properties");

    this.verifyApiClient(connectionId);

    this.isSessionInitialized = true;
  }

  private void trackStartupPerformance() {
    final Instant instanceStart = Clock.systemUTC().instant();
    PerformanceTracker performanceTracker = new PerformanceTracker();
    JavabuilderContext.getInstance().register(PerformanceTracker.class, performanceTracker);
    if (coldBoot) {
      performanceTracker.trackColdBoot(COLD_BOOT_START, COLD_BOOT_END, instanceStart);
      coldBoot = false;
    } else {
      performanceTracker.trackInstanceStart(instanceStart);
    }
  }

  /**
   * Create an {@link OutputAdapter} for the session.
   *
   * @throws InternalFacingException if the OutputAdapter cannot be created from the given input.
   */
  private OutputAdapter createOutputAdapter(Map<String, String> lambdaInput)
      throws InternalFacingException {
    final String connectionId = lambdaInput.get("connectionId");
    if (connectionId == null) {
      throw new InternalFacingException(INVALID_INPUT, new Exception("Missing connection ID"));
    }
    final AWSOutputAdapter awsOutputAdapter = new AWSOutputAdapter(connectionId, this.apiClient);

    try {
      final ExecutionType executionType = ExecutionType.valueOf(lambdaInput.get("executionType"));
      if (executionType == ExecutionType.TEST) {
        return new UserTestOutputAdapter(awsOutputAdapter);
      }
      return awsOutputAdapter;
    } catch (IllegalArgumentException e) {
      throw new InternalFacingException(INVALID_INPUT, e);
    }
  }

  /**
   * Clear the container's temp directory in preparation for code execution. Throws a {@link
   * FatalError} if any IOException occurs to force the container to shutdown and release resources.
   */
  private void clearTempDirectory(TempDirectoryManager tempDirectoryManager) {
    try {
      // Log disk space before clearing the directory
      LoggerUtils.sendDiskSpaceReport();
      tempDirectoryManager.cleanUpTempDirectory(null);
    } catch (IOException e) {
      // Wrap this in our error type so we can log it and tell the user.
      throw new FatalError(FatalErrorKey.TEMP_DIRECTORY_CLEANUP_ERROR, e);
    }
  }

  /** Creates the {@link CodeExecutionManager} for building and executing code. */
  private CodeExecutionManager createExecutionManager(
      Map<String, String> lambdaInput,
      Context context,
      String connectionId,
      OutputAdapter outputAdapter,
      TempDirectoryManager tempDirectoryManager)
      throws InternalServerException {
    final String queueUrl = lambdaInput.get("queueUrl");
    final String queueName = lambdaInput.get("queueName");
    final ExecutionType executionType = ExecutionType.valueOf(lambdaInput.get("executionType"));
    final JSONObject options = new JSONObject(lambdaInput.get("options"));
    final String javabuilderSessionId = lambdaInput.get("javabuilderSessionId");
    final List<String> compileList = JSONUtils.listFromJSONObjectMember(options, "compileList");

    final AWSInputAdapter inputAdapter = new AWSInputAdapter(SQS_CLIENT, queueUrl, queueName);
    final AWSContentManager contentManager =
        new AWSContentManager(
            S3_CLIENT, CONTENT_BUCKET_NAME, javabuilderSessionId, CONTENT_BUCKET_URL, context);

    return new CodeExecutionManager(
        contentManager.getProjectFileLoader(),
        inputAdapter,
        outputAdapter,
        executionType,
        compileList,
        tempDirectoryManager,
        contentManager,
        new AWSSystemExitHelper(connectionId, this.apiClient));
  }

  /**
   * Cleans up resources used by the current invocation, and prepares the container for the next
   * invocation.
   */
  private void shutDown(
      CodeExecutionManager executionManager,
      String connectionId,
      AmazonApiGatewayManagementApi api) {
    // No need to shut down if the session is not initialized. This means that we've already shut
    // down (ex. due to timeout) or this method was somehow called out of turn.
    if (!this.isSessionInitialized) {
      return;
    }

    if (executionManager != null) {
      try {
        executionManager.shutDown();
      } catch (Throwable e) {
        // Catch any exceptions thrown during shutdown; the program has already terminated,
        // so these don't need to be reported to the user.
        final InternalFacingRuntimeException internal =
            new InternalFacingRuntimeException("Exception during shutdown", e);
        Logger.getLogger(MAIN_LOGGER).warning(internal.getLoggingString());
      }
    }

    if (this.unhealthyContainerChecker.shouldForceRecycleContainer(LAMBDA_ID)) {
      System.exit(LambdaErrorCodes.UNHEALTHY_CONTAINER_ERROR_CODE);
    }

    PerformanceTracker performanceTracker =
        (PerformanceTracker) JavabuilderContext.getInstance().get(PerformanceTracker.class);
    performanceTracker.trackInstanceEnd();
    performanceTracker.logPerformance();
    JavabuilderContext.getInstance().destroyAndReset();

    this.cleanUpAWSResources(connectionId, api);

    File f = Paths.get(System.getProperty("java.io.tmpdir")).toFile();
    if ((double) f.getUsableSpace() / f.getTotalSpace() < 0.5) {
      // The current project holds a lock on too many resources. Force the JVM to quit in
      // order to release the resources for the next use of the container.
      System.exit(LambdaErrorCodes.LOW_DISK_SPACE_ERROR_CODE);
    }

    this.isSessionInitialized = false;
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
                LambdaUtils.safelySendMessage(
                    outputAdapter, new StatusMessage(StatusMessageKey.TIMEOUT_WARNING), true);
                timeoutWarningSent = true;
              }

              if (context.getRemainingTimeInMillis() < TIMEOUT_CLEANUP_BUFFER_MS) {
                LambdaUtils.safelySendMessage(
                    outputAdapter, new StatusMessage(StatusMessageKey.TIMEOUT), true);
                // Shut down the environment
                this.shutDown(codeExecutionManager, connectionId, api);
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
  private void cleanUpAWSResources(String connectionId, AmazonApiGatewayManagementApi api) {
    final DeleteConnectionRequest deleteConnectionRequest =
        new DeleteConnectionRequest().withConnectionId(connectionId);
    // Deleting the API Gateway connection should always be the last thing executed because the
    // delete action cleans up the AWS resources associated with this lambda
    try {
      api.deleteConnection(deleteConnectionRequest);
    } catch (GoneException e) {
      // if the connection is already gone, we don't need to delete the connection.
    } catch (Exception e) {
      // Handle any other exceptions so that shut down proceeds normally. If this is an
      // IllegalStateException, it indicates that the connection was already shut down for
      // some reason.
      Logger.getLogger(MAIN_LOGGER).warning(JavabuilderThrowableMessageUtils.getLoggingString(e));
    }
    // clean up log handler to avoid duplicate logs in future runs.
    Handler[] allHandlers = Logger.getLogger(MAIN_LOGGER).getHandlers();
    for (int i = 0; i < allHandlers.length; i++) {
      Logger.getLogger(MAIN_LOGGER).removeHandler(allHandlers[i]);
    }
  }

  private void verifyApiClient(String connectionId) {
    GetConnectionRequest connectionRequest =
        new GetConnectionRequest().withConnectionId(connectionId);
    try {
      this.apiClient.getConnection(connectionRequest);
    } catch (IllegalStateException e) {
      // This can occur if the api client has been shut down, which we have seen happen on occasion.
      // Recreate the api client in this case. Log a warning so we can track when this happens.
      Logger.getLogger(MAIN_LOGGER)
          .warning(
              "Received illegal state exception when trying to talk to API Gateway. Recreating api client.");
      this.apiClient =
          AmazonApiGatewayManagementApiClientBuilder.standard()
              .withEndpointConfiguration(
                  new AwsClientBuilder.EndpointConfiguration(API_ENDPOINT, "us-east-1"))
              // .withMonitoringListener(new JavabuilderMonitoringListener())
              .build();
    }
  }
}
