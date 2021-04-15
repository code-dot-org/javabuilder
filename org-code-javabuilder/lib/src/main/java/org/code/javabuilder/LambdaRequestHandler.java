package org.code.javabuilder;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import java.util.Map;

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

    // Create file manager
    final UserProjectFileManager userProjectFileManager = new UserProjectFileManager(projectUrl);

    // Create and invoke the code execution environment
    try (CodeBuilder codeBuilder =
        new CodeBuilder(inputAdapter, outputAdapter, userProjectFileManager)) {
      codeBuilder.compileUserCode();
      codeBuilder.runUserCode();
    } catch (UserFacingException e) {
      // Send user-facing exceptions to the user and log the stack trace to CloudWatch
      outputAdapter.sendMessage(e.getMessage());
      context.getLogger().log(e.getLoggingString());
    } catch (UserInitiatedException e) {
      // Send user-facing exceptions to the user and log the stack trace to CloudWatch
      outputAdapter.sendMessage(e.getMessage());
      context.getLogger().log(e.getLoggingString());
    } catch (InternalFacingException e) {
      // Send internal-facing exceptions to CloudWatch
      context.getLogger().log(e.getLoggingString());
    } catch (Exception e) {
      e.printStackTrace();
    }

    return "done";
  }
}
