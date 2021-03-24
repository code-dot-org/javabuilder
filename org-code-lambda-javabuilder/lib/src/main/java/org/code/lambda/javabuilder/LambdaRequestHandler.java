package org.code.lambda.javabuilder;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

//https://github.com/awsdocs/aws-lambda-developer-guide/tree/main/sample-apps/blank-java
public class LambdaRequestHandler implements RequestHandler<Map<String,String>, String>{
  @Override
  public String handleRequest(Map<String, String> lambdaInput, Context context) {
    final String connectionId = lambdaInput.get("connectionId");
    final String apiEndpoint = lambdaInput.get("apiEndpoint");
    final String queueUrl = lambdaInput.get("queueUrl");
    JavaBuilder javaBuilder = new JavaBuilder(connectionId, apiEndpoint, queueUrl);
    javaBuilder.runUserCode();

    return "done";
  }
}
