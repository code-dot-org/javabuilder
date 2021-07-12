package dev.javabuilder;

import java.io.IOException;
import java.io.InputStream;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.code.protocol.ClientMessage;
import org.code.protocol.JavabuilderException;
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
    try {
      endpoint.sendText(message.getFormattedMessage());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String writeToFile(String filename, InputStream input) throws JavabuilderException {
    return null;
  }

  public void sendDebuggingMessage(ClientMessage message) {
    try {
      endpoint.sendText(message.getFormattedMessage());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
