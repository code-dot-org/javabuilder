package dev.javabuilder;

import java.io.IOException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.code.javabuilder.OutputAdapter;

/**
 * Intended for local testing with dashboard only. Passes output to the provided WebSocket session
 */
public class WebSocketOutputAdapter implements OutputAdapter {
  private final RemoteEndpoint.Basic endpoint;

  public WebSocketOutputAdapter(Session session) {
    this.endpoint = session.getBasicRemote();
  }

  @Override
  public void sendMessage(String message) {
    try {
      endpoint.sendText(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
