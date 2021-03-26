package org.code.lambda.javabuilder;

import java.nio.ByteBuffer;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;

public class OutputHandler extends Thread {
  private static String connectionId;
  private static AmazonApiGatewayManagementApi api;

  public OutputHandler(String connectionId, AmazonApiGatewayManagementApi api){
    this.connectionId = connectionId;
    this.api = api;
  }
  public static void sendDebuggingMessage(String message) {
//    String time = String.valueOf(java.time.Clock.systemUTC().instant());
//    PostToConnectionRequest post = new PostToConnectionRequest();
//    post.setConnectionId(connectionId);
//    post.setData(ByteBuffer.wrap((message + " " + time).getBytes()));
//    api.postToConnection(post);
  }

  public static void sendMessage(String message) {
    PostToConnectionRequest post = new PostToConnectionRequest();
    post.setConnectionId(connectionId);
    post.setData(ByteBuffer.wrap((message).getBytes()));
    api.postToConnection(post);
  }
}
