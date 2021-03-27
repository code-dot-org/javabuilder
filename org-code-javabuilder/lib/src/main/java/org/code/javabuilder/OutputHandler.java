package org.code.javabuilder;

import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import java.nio.ByteBuffer;

public class OutputHandler extends Thread {
  private final String connectionId;
  private final AmazonApiGatewayManagementApi api;

  public OutputHandler(String connectionId, AmazonApiGatewayManagementApi api) {
    this.connectionId = connectionId;
    this.api = api;
  }

  public void sendDebuggingMessage(String message) {
    String time = String.valueOf(java.time.Clock.systemUTC().instant());
    PostToConnectionRequest post = new PostToConnectionRequest();
    post.setConnectionId(connectionId);
    post.setData(ByteBuffer.wrap((message + " " + time).getBytes()));
    api.postToConnection(post);
  }

  public void sendMessage(String message) {
    PostToConnectionRequest post = new PostToConnectionRequest();
    post.setConnectionId(connectionId);
    post.setData(ByteBuffer.wrap((message).getBytes()));
    api.postToConnection(post);
  }
}
