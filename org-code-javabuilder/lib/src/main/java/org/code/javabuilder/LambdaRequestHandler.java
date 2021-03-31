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
 *
 */
// https://github.com/awsdocs/aws-lambda-developer-guide/tree/main/sample-apps/blank-java
public class LambdaRequestHandler implements RequestHandler<Map<String,String>, String>{
  @Override
  public String handleRequest(Map<String, String> lambdaInput, Context context) {
    // The lambda handler should have minimal application logic: https://docs.aws.amazon.com/lambda/latest/dg/best-practices.html
    final String connectionId = lambdaInput.get("connectionId");
    final String apiEndpoint = lambdaInput.get("apiEndpoint");
    final String queueUrl = lambdaInput.get("queueUrl");

    AmazonApiGatewayManagementApi api = AmazonApiGatewayManagementApiClientBuilder.standard().withEndpointConfiguration(
        new AwsClientBuilder.EndpointConfiguration(apiEndpoint, "us-east-1")
    ).build();
    final AWSOutputAdapter outputAdapter = new AWSOutputAdapter(connectionId, api);

    final AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();
    final AWSInputAdapter inputAdapter = new AWSInputAdapter(sqsClient, queueUrl);

    JavaBuilder javaBuilder = new JavaBuilder(inputAdapter, outputAdapter);
    javaBuilder.runUserCode();

    return "done";
  }
}
