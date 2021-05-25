package org.code.javabuilder;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import java.util.Map;
import org.code.protocol.JavabuilderError;
import org.code.protocol.JavabuilderException;
import org.code.protocol.Properties;
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
    final String projectUrl = lambdaInput.get("projectUrl");
    final String levelId = lambdaInput.get("levelId");
    final String dashboardHostname = "https://" + lambdaInput.get("iss");
    final JSONObject options = new JSONObject(lambdaInput.get("options"));
    boolean useNeighborhood = false;
    if (options.has("useNeighborhood")) {
      String useNeighborhoodStr = options.getString("useNeighborhood");
      useNeighborhood = Boolean.parseBoolean(useNeighborhoodStr);
    }

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

    // Create file loader
    final UserProjectFileLoader userProjectFileLoader =
        new UserProjectFileLoader(projectUrl, levelId, dashboardHostname, useNeighborhood);

    // Load files to memory and create and invoke the code execution environment
    try {
      UserProjectFiles userProjectFiles = userProjectFileLoader.loadFiles();
      try (CodeBuilder codeBuilder =
          new CodeBuilder(inputAdapter, outputAdapter, userProjectFiles)) {
        codeBuilder.buildUserCode();
        codeBuilder.runUserCode();
      }
    } catch (JavabuilderException | JavabuilderError e) {
      // Send user-facing exceptions to the user and log the stack trace to CloudWatch
      outputAdapter.sendMessage(e.getExceptionMessage());
      context.getLogger().log(e.getLoggingString());
    } catch (InternalFacingException e) {
      // Send internal-facing exceptions to CloudWatch
      context.getLogger().log(e.getLoggingString());
    } catch (Throwable e) {
      e.printStackTrace();
    }

    return "done";
  }
}
