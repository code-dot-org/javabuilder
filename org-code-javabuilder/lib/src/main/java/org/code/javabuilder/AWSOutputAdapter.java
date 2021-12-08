package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.model.GetConnectionRequest;
import com.amazonaws.services.apigatewaymanagementapi.model.GoneException;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import org.code.protocol.*;
import org.json.JSONObject;

/** Sends messages to Amazon API Gateway from the user's program. */
public class AWSOutputAdapter implements OutputAdapter {
  private final String connectionId;
  private final AmazonApiGatewayManagementApi api;
  private boolean hasActiveConnection;

  public AWSOutputAdapter(String connectionId, AmazonApiGatewayManagementApi api) {
    this.connectionId = connectionId;
    this.api = api;
    this.hasActiveConnection = true;
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
    post.setData(ByteBuffer.wrap((message.getFormattedMessage()).getBytes()));
    this.sendMessageHelper(post);
  }

  /**
   * Check if we still have an active connection to AWS.
   *
   * @return boolean
   */
  @Override
  public boolean hasActiveConnection() {
    if (!this.hasActiveConnection) {
      return false;
    }
    try {
      // The simplest way to find out if we have an active connection is to attempt to get
      // a connection.
      GetConnectionRequest connectionRequest = new GetConnectionRequest();
      connectionRequest.setConnectionId(connectionId);
      this.api.getConnection(connectionRequest);
    } catch (GoneException e) {
      this.hasActiveConnection = false;
      this.sendGoneLog(e);
    }
    return this.hasActiveConnection;
  }

  public void sendDebuggingMessage(ClientMessage message) {
    String time = String.valueOf(java.time.Clock.systemUTC().instant());
    PostToConnectionRequest post = new PostToConnectionRequest();
    post.setConnectionId(connectionId);
    post.setData(ByteBuffer.wrap((message + " " + time).getBytes()));
    this.sendMessageHelper(post);
  }

  private void sendMessageHelper(PostToConnectionRequest post) {
    if (!this.hasActiveConnection) {
      return;
    }
    try {
      this.api.postToConnection(post);
    } catch (GoneException e) {
      this.hasActiveConnection = false;
      this.sendGoneLog(e);
    }
  }

  private void sendGoneLog(GoneException error) {
    // Log the error
    JSONObject eventData = new JSONObject();
    eventData.put("exceptionMessage", error.getMessage());
    eventData.put("type", error.getClass().getSimpleName());
    Logger.getLogger(MAIN_LOGGER).severe(eventData.toString());
  }
}
