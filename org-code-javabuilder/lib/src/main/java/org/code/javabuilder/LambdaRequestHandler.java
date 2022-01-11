package org.code.javabuilder;

import static org.code.protocol.InternalErrorKey.INTERNAL_EXCEPTION;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.code.protocol.*;
import org.code.protocol.LoggerUtils.ClearStatus;
import org.code.protocol.LoggerUtils.SessionTime;
import org.json.JSONObject;

/**
 * This is the entry point for the lambda function. This should be thought of as similar to a main
 * function, but with a specific input contract.
 * https://github.com/awsdocs/aws-lambda-developer-guide/tree/main/sample-apps/blank-java
 */
public class LambdaRequestHandler implements RequestHandler<Map<String, String>, String> {

  private static final int CHECK_THREAD_INTERVAL_MS = 500;
  private static final int TIMEOUT_WARNING_MS = 20000;
  private static final int TIMEOUT_CLEANUP_BUFFER_MS = 5000;
  private static final String LAMBDA_ID = UUID.randomUUID().toString();
  // This is an error code we made up to signal that available disk space is less than 50%.
  // It may be used in tracking on Cloud Watch.
  private static final int LOW_DISK_SPACE_ERROR_CODE = 50;

  public LambdaRequestHandler() {
    // create CachedResources once for the entire container.
    // This will only be called once in the initial creation of the lambda instance.
    // Documentation: https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html
    CachedResources.create();
  }

  /**
   * This is the implementation of the long-running-lambda where user code will be compiled and
   * executed.
   *
   * @param lambdaInput A map of the json input to this lambda function. Input is formatted like
   *     this: { "queueUrl": <session-specific-url>, "apiEndpoint": <environment-specific-url>,
   *     "connectionId": <session-specific-string>, "channelId": <project-specific-string> }
   * @param context Currently unused, this is the context in which the lambda was invoked.
   * @return A string. Right now, we aren't using this, so anything can be returned.
   */
  @Override
  public String handleRequest(Map<String, String> lambdaInput, Context context) {
    // The lambda handler should have minimal application logic:
    // https://docs.aws.amazon.com/lambda/latest/dg/best-practices.html
    final String connectionId = lambdaInput.get("connectionId");
    final String apiEndpoint = lambdaInput.get("apiEndpoint");
    final String queueUrl = lambdaInput.get("queueUrl");
    final String queueName = lambdaInput.get("queueName");
    final String levelId = lambdaInput.get("levelId");
    final String channelId = lambdaInput.get("channelId");
    final String miniAppType = lambdaInput.get("miniAppType");
    final ExecutionType executionType = ExecutionType.valueOf(lambdaInput.get("executionType"));
    final String dashboardHostname = "https://" + lambdaInput.get("iss");
    final JSONObject options = new JSONObject(lambdaInput.get("options"));
    final String javabuilderSessionId = lambdaInput.get("javabuilderSessionId");
    final String outputBucketName = System.getenv("OUTPUT_BUCKET_NAME");
    final String getOutputUrl = System.getenv("GET_OUTPUT_URL");
    final boolean useNeighborhood =
        JSONUtils.booleanFromJSONObjectMember(options, "useNeighborhood");
    final List<String> compileList = JSONUtils.listFromJSONObjectMember(options, "compileList");

    Logger logger = Logger.getLogger(MAIN_LOGGER);
    logger.addHandler(
        new LambdaLogHandler(
            context.getLogger(),
            javabuilderSessionId,
            connectionId,
            levelId,
            LambdaRequestHandler.LAMBDA_ID,
            channelId,
            miniAppType));
    // turn off the default console logger
    logger.setUseParentHandlers(false);

    Properties.setConnectionId(connectionId);

    // Create user-program output handlers
    AmazonApiGatewayManagementApi api =
        AmazonApiGatewayManagementApiClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(apiEndpoint, "us-east-1"))
            .build();
    final AWSOutputAdapter outputAdapter = new AWSOutputAdapter(connectionId, api);

    // Create user input handlers
    final AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();
    final AWSInputAdapter inputAdapter = new AWSInputAdapter(sqsClient, queueUrl, queueName);

    final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
    final AWSFileManager fileManager =
        new AWSFileManager(s3Client, outputBucketName, javabuilderSessionId, getOutputUrl, context);
    // TODO: Move common setup steps into CodeExecutionManager#onPreExecute
    GlobalProtocol.create(
        outputAdapter, inputAdapter, dashboardHostname, channelId, levelId, fileManager);

    // Create file loader
    final UserProjectFileLoader userProjectFileLoader =
        new UserProjectFileLoader(
            GlobalProtocol.getInstance().generateSourcesUrl(),
            levelId,
            dashboardHostname,
            useNeighborhood);

    // manually set font configuration file since there is no font configuration on a lambda.
    java.util.Properties props = System.getProperties();
    // /opt is the folder all layer files go into.
    props.put("sun.awt.fontconfig", "/opt/fontconfig.properties");

    try {
      // Log disk space before clearing the directory
      LoggerUtils.sendDiskSpaceReport();

      LoggerUtils.sendDiskSpaceUpdate(SessionTime.START_SESSION, ClearStatus.BEFORE_CLEAR);
      fileManager.cleanUpTempDirectory(null);
      LoggerUtils.sendDiskSpaceUpdate(SessionTime.START_SESSION, ClearStatus.AFTER_CLEAR);
    } catch (IOException e) {
      // Wrap this in our error type so we can log it and tell the user.
      InternalServerError error = new InternalServerError(INTERNAL_EXCEPTION, e);

      // Log the error
      LoggerUtils.logError(error);

      // This affected the user. Let's tell them about it.
      outputAdapter.sendMessage(error.getExceptionMessage());

      cleanUpResources(connectionId, api);
      return "error clearing tmpdir";
    }

    final CodeExecutionManager codeExecutionManager =
        new CodeExecutionManager(
            userProjectFileLoader,
            GlobalProtocol.getInstance().getInputHandler(),
            outputAdapter,
            executionType,
            compileList,
            fileManager);

    final Thread timeoutNotifierThread =
        createTimeoutThread(context, outputAdapter, codeExecutionManager, connectionId, api);
    timeoutNotifierThread.start();

    try {
      // start code build and block until completed
      codeExecutionManager.execute();
    } catch (Throwable e) {
      // All errors should be caught, but if for any reason we encounter an error here, make sure we
      // catch it, log, and always clean up resources
      LoggerUtils.logException(e);
    } finally {
      // Stop timeout listener and clean up
      timeoutNotifierThread.interrupt();
      cleanUpResources(connectionId, api);
      File f = Paths.get(System.getProperty("java.io.tmpdir")).toFile();
      if ((double) f.getUsableSpace() / f.getTotalSpace() < 0.5) {
        // The current project holds a lock on too many resources. Force the JVM to quit in
        // order to release the resources for the next use of the container.
        System.exit(LOW_DISK_SPACE_ERROR_CODE);
      }
    }

    return "done";
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

  private void cleanUpResources(String connectionId, AmazonApiGatewayManagementApi api) {
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
