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
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.code.protocol.*;
import org.json.JSONObject;

/**
 * This is the entry point for the lambda function. This should be thought of as similar to a main
 * function, but with a specific input contract.
 * https://github.com/awsdocs/aws-lambda-developer-guide/tree/main/sample-apps/blank-java
 */
public class LambdaRequestHandler implements RequestHandler<Map<String, String>, String> {

  private static final int CHECK_THREAD_INTERVAL_MS = 500;
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
            context.getLogger(), javabuilderSessionId, connectionId, levelId, channelId));
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
    final AWSFileWriter fileWriter =
        new AWSFileWriter(s3Client, outputBucketName, javabuilderSessionId, getOutputUrl);
    GlobalProtocol.create(
        outputAdapter, inputAdapter, dashboardHostname, channelId, levelId, fileWriter);

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
      // Delete any leftover contents of the tmp folder from previous lambda invocations
      Util.recursivelyClearDirectory(Paths.get(System.getProperty("java.io.tmpdir")));
    } catch (IOException e) {
      // Wrap this in our error type so we can log it and tell the user.
      InternalServerError error = new InternalServerError(INTERNAL_EXCEPTION, e);

      // Log the error
      JSONObject eventData = new JSONObject();
      eventData.put("exceptionMessage", error.getExceptionMessage());
      eventData.put("loggingString", error.getLoggingString());
      Logger.getLogger(MAIN_LOGGER).severe(eventData.toString());

      // This affected the user. Let's tell them about it.
      outputAdapter.sendMessage(error.getExceptionMessage());

      cleanUpResources(connectionId, api);
      return "error clearing tmpdir";
    }

    try {
      // Load files to memory and create and invoke the code execution environment
      CodeBuilderRunnable codeBuilderRunnable =
          new CodeBuilderRunnable(userProjectFileLoader, outputAdapter, executionType, compileList);
      Thread codeBuilderThread = new Thread(codeBuilderRunnable);
      // start code build and execute in a thread
      codeBuilderThread.start();
      while (codeBuilderThread.isAlive()) {
        // sleep for CHECK_THREAD_INTERVAL_MS, then check if we've lost the connection to the
        // input or output adapter. This means we have lost connection to the end user (either
        // because they terminated their program or some other issue), and we should stop
        // executing their code.
        Thread.sleep(CHECK_THREAD_INTERVAL_MS);
        if (codeBuilderThread.isAlive()
            && (!inputAdapter.hasActiveConnection() || !outputAdapter.hasActiveConnection())) {
          codeBuilderThread.interrupt();
          break;
        }
      }
    } catch (InterruptedException interruptedException) {
      // no-op if we have an interrupted exception, as it happened due to a user ending their
      // program.
    } finally {
      cleanUpResources(connectionId, api);
    }
    return "done";
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
