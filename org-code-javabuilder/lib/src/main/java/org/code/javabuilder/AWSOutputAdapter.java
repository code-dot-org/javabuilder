package org.code.javabuilder;

import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import java.nio.ByteBuffer;

/** Sends messages to Amazon API Gateway from the user's program. */
public class AWSOutputAdapter implements OutputAdapter {
  private final String connectionId;
  private final AmazonApiGatewayManagementApi api;

  public AWSOutputAdapter(String connectionId, AmazonApiGatewayManagementApi api) {
    this.connectionId = connectionId;
    this.api = api;
  }

  /**
   * POSTs a message to the API Gateway @connections url for the current user
   *
   * @param message The message to send to API Gateway from the user's program.
   */
  public void sendMessage(ClientMessage message) {
    PostToConnectionRequest post = new PostToConnectionRequest();
    post.setConnectionId(connectionId);
    message.addDetail("connectionId", connectionId);
    post.setData(ByteBuffer.wrap((message.getFormattedMessage()).getBytes()));
    api.postToConnection(post);
  }

  public void sendDebuggingMessage(ClientMessage message) {
    String time = String.valueOf(java.time.Clock.systemUTC().instant());
    PostToConnectionRequest post = new PostToConnectionRequest();
    post.setConnectionId(connectionId);
    message.addDetail("connectionId", connectionId);
    post.setData(ByteBuffer.wrap((message + " " + time).getBytes()));
    api.postToConnection(post);
  }
}
