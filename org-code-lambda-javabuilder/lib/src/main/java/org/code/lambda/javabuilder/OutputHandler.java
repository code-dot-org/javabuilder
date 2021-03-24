package org.code.lambda.javabuilder;

import java.nio.ByteBuffer;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;

public class OutputHandler extends Thread {
  private final String APIGatewayConnection = "https://qd6izwqumb.execute-api.us-east-1.amazonaws.com/development/@connections";
  private String url = "";
  private String connectionId = "";
  private static final String queueUrl = "https://sqs.us-east-1.amazonaws.com/165336972514/JavaBuilderTestQueue.fifo";
  private final AmazonApiGatewayManagementApi api;
  private boolean messageQueueActive = false;

  private AmazonSQS sqsClient = null;
  public OutputHandler(String connectionId, AmazonApiGatewayManagementApi api){
    this.url = this.APIGatewayConnection + '/' + connectionId;
    this.connectionId = connectionId;
    this.api = api;
  }

  public void sendMessage(String message) {
    PostToConnectionRequest post = new PostToConnectionRequest();
    post.setConnectionId(connectionId);
    post.setData(ByteBuffer.wrap((message).getBytes()));
    api.postToConnection(post);
    OutputSemaphore.decreaseOutputInProgress();
  }
}
