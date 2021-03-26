package org.code.lambda.javabuilder;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

// https://github.com/awsdocs/aws-lambda-developer-guide/tree/main/sample-apps/blank-java
public class LambdaRequestHandler implements RequestHandler<Map<String,String>, String>{
  @Override
  public String handleRequest(Map<String, String> lambdaInput, Context context) {
    // The lambda handler should have minimal application logic: https://docs.aws.amazon.com/lambda/latest/dg/best-practices.html
    final String connectionId = lambdaInput.get("connectionId");
    final String apiEndpoint = lambdaInput.get("apiEndpoint");
    final String queueUrl = lambdaInput.get("queueUrl");
    JavaBuilder javaBuilder = new JavaBuilder(connectionId, apiEndpoint, queueUrl);
    javaBuilder.runUserCode();

    return "done";
  }
}
