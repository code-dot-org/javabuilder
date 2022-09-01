package dev.javabuilder;

import java.io.IOException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.code.protocol.ClientMessage;
import org.code.protocol.InternalExceptionKey;
import org.code.protocol.InternalServerRuntimeException;
import org.code.protocol.OutputAdapter;

/**
 * Intended for local testing with dashboard only. Passes output to the provided WebSocket session
 */
public class WebSocketOutputAdapter implements OutputAdapter {
  private final RemoteEndpoint.Basic endpoint;

  public WebSocketOutputAdapter(Session session) {
    this.endpoint = session.getBasicRemote();
  }

  @Override
  public void sendMessage(ClientMessage message) {
    if (message.shouldSendInRunMode()) {
      try {
        endpoint.sendText(message.getFormattedMessage());
      } catch (IOException e) {
        e.printStackTrace();
      } catch (IllegalStateException e) {
        throw new InternalServerRuntimeException(InternalExceptionKey.CONNECTION_TERMINATED, e);
      }
    }
  }

  public void sendDebuggingMessage(ClientMessage message) {
    if (message.shouldSendInRunMode()) {
      try {
        endpoint.sendText(message.getFormattedMessage());
      } catch (IOException e) {
        e.printStackTrace();
      } catch (IllegalStateException e) {
        throw new InternalServerRuntimeException(InternalExceptionKey.CONNECTION_TERMINATED, e);
      }
    }
  }
}
