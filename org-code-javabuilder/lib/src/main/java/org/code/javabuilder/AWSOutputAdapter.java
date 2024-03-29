package org.code.javabuilder;

import static org.code.javabuilder.InternalFacingExceptionTypes.CONNECTION_TERMINATED;

import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.model.GoneException;
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
    if (message.shouldAlwaysSend()) {
      PostToConnectionRequest post = new PostToConnectionRequest();
      post.setConnectionId(connectionId);
      post.setData(ByteBuffer.wrap((message.getFormattedMessage()).getBytes()));
      this.sendMessageHelper(post);
    }
  }

  public void sendDebuggingMessage(ClientMessage message) {
    if (message.shouldAlwaysSend()) {
      String time = String.valueOf(java.time.Clock.systemUTC().instant());
      PostToConnectionRequest post = new PostToConnectionRequest();
      post.setConnectionId(connectionId);
      post.setData(ByteBuffer.wrap((message + " " + time).getBytes()));
      this.sendMessageHelper(post);
    }
  }

  private void sendMessageHelper(PostToConnectionRequest post) {
    try {
      this.api.postToConnection(post);
    } catch (GoneException e) {
      throw new InternalFacingRuntimeException(CONNECTION_TERMINATED, e);
    } catch (IllegalStateException e) {
      // Thrown when the API Gateway client has been unexpectedly shut down.
      // We are still actively investigating why this happens in the first place,
      // but this will make the container fail for all subsequent sessions, so it
      // should be recycled.
      throw new FatalError(FatalErrorKey.CONNECTION_POOL_SHUT_DOWN, e);
    }
  }
}
