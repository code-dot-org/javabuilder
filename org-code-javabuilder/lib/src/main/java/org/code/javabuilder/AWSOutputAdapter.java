package org.code.javabuilder;

import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.model.GoneException;
import com.amazonaws.services.apigatewaymanagementapi.model.PayloadTooLargeException;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import java.nio.ByteBuffer;
import org.code.protocol.*;

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
  @Override
  public void sendMessage(ClientMessage message) {
    PostToConnectionRequest post = new PostToConnectionRequest();
    post.setConnectionId(connectionId);

    try {
      String fullMessage = message.getFormattedMessage();
      post.setData(ByteBuffer.wrap(fullMessage.getBytes()));
      this.sendMessageHelper(post);
    } catch (PayloadTooLargeException e) {
      String shortenedMessage = message.getShortenedFormattedMessage();
      post.setData(ByteBuffer.wrap(shortenedMessage.getBytes()));
      this.sendMessageHelper(post);
    }
  }

  public void sendDebuggingMessage(ClientMessage message) {
    String time = String.valueOf(java.time.Clock.systemUTC().instant());
    PostToConnectionRequest post = new PostToConnectionRequest();
    post.setConnectionId(connectionId);
    post.setData(ByteBuffer.wrap((message + " " + time).getBytes()));
    this.sendMessageHelper(post);
  }

  private void sendMessageHelper(PostToConnectionRequest post) {
    try {
      this.api.postToConnection(post);
    } catch (GoneException e) {
      throw new InternalServerRuntimeError(InternalErrorKey.CONNECTION_TERMINATED, e);
    }
  }
}
