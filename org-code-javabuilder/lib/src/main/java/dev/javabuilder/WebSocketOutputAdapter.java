package dev.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.io.IOException;
import java.util.logging.Logger;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.code.protocol.ClientMessage;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.InternalServerRuntimeError;
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
    Logger.getLogger(MAIN_LOGGER).info("Sending output message: " + message.getFormattedMessage());
    try {
      endpoint.sendText(message.getFormattedMessage());
    } catch (IOException e) {
      e.printStackTrace();
    } catch (IllegalStateException e) {
      throw new InternalServerRuntimeError(InternalErrorKey.CONNECTION_TERMINATED, e);
      // this happens if the endpoint has been closed. Fail silently.
    }
  }

  @Override
  public boolean hasActiveConnection() {
    return true;
  }

  public void sendDebuggingMessage(ClientMessage message) {
    try {
      endpoint.sendText(message.getFormattedMessage());
    } catch (IOException e) {
      e.printStackTrace();
    } catch (IllegalStateException e) {
      throw new InternalServerRuntimeError(InternalErrorKey.CONNECTION_TERMINATED, e);
      // this happens if the endpoint has been closed. Fail silently.
    }
  }
}
