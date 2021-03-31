package org.code.javabuilder;

import java.nio.ByteBuffer;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;

public class AWSOutputAdapter implements OutputAdapter {
  private final String connectionId;
  private final AmazonApiGatewayManagementApi api;

  private static String s_connectionId;
  private static AmazonApiGatewayManagementApi s_api;

  public AWSOutputAdapter(String connectionId, AmazonApiGatewayManagementApi api){
    this.connectionId = connectionId;
    this.api = api;
    this.s_connectionId = connectionId;
    this.s_api = api;
  }

  public static void sendDebuggingMessage(String message) {
    String time = String.valueOf(java.time.Clock.systemUTC().instant());
    PostToConnectionRequest post = new PostToConnectionRequest();
    post.setConnectionId(s_connectionId);
    post.setData(ByteBuffer.wrap((message + " " + time).getBytes()));
    s_api.postToConnection(post);
  }

  public void sendMessage(String message) {
    PostToConnectionRequest post = new PostToConnectionRequest();
    post.setConnectionId(connectionId);
    post.setData(ByteBuffer.wrap((message).getBytes()));
    api.postToConnection(post);
  }
}
