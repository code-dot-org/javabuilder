package dev.javabuilder;

import org.code.javabuilder.OutputAdapter;

import javax.websocket.RemoteEndpoint;
import java.io.IOException;

public class WebSocketOutputAdapter implements OutputAdapter {
  private WebSocketServer server;

  public WebSocketOutputAdapter(WebSocketServer server) {
    this.server = server;
  }

  @Override
  public void sendMessage(String message) {
    while(server.getSession() == null) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    RemoteEndpoint.Basic other = server.getSession().getBasicRemote();
    try {
      other.sendText(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
