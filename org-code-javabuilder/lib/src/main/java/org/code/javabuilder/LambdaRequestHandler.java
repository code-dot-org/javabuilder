package org.code.javabuilder;

import static org.code.protocol.InternalErrorKey.INTERNAL_EXCEPTION;
import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.DeleteConnectionRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Logger;
import org.code.protocol.*;
import org.json.JSONObject;

/**
 * This is the entry point for the lambda function. This should be thought of as similar to a main
 * function, but with a specific input contract.
 * https://github.com/awsdocs/aws-lambda-developer-guide/tree/main/sample-apps/blank-java
 */
public class LambdaRequestHandler implements RequestHandler<Map<String, String>, String> {
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
    final String levelId = lambdaInput.get("levelId");
    final String channelId = lambdaInput.get("channelId");
    final String dashboardHostname = "https://" + lambdaInput.get("iss");
    final JSONObject options = new JSONObject(lambdaInput.get("options"));
    final String javabuilderSessionId = lambdaInput.get("javabuilderSessionId");
    final String outputBucketName = System.getenv("OUTPUT_BUCKET_NAME");
    final String getOutputUrl = System.getenv("GET_OUTPUT_URL");
    boolean useNeighborhood = false;
    if (options.has("useNeighborhood")) {
      String useNeighborhoodStr = options.getString("useNeighborhood");
      useNeighborhood = Boolean.parseBoolean(useNeighborhoodStr);
    }

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
    final AWSInputAdapter inputAdapter = new AWSInputAdapter(sqsClient, queueUrl);

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
      CodeBuilderWrapper codeBuilderWrapper =
          new CodeBuilderWrapper(userProjectFileLoader, outputAdapter);
      codeBuilderWrapper.executeCodeBuilder();
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
    api.deleteConnection(deleteConnectionRequest);
  }
}
