package dev.javabuilder;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/javabuilder")
public class WebSocketServer {
//  private WebSocketInputAdapter inputAdapter;
//  private Session session;

//  public WebSocketServer(WebSocketInputAdapter inputAdapter) {
//    this.inputAdapter = inputAdapter;
//  }

  @OnOpen
  public void myOnOpen (Session session) {
    WebSocketConfig.setSession(session);
  }

  @OnClose
  public void myOnClose (Session session) {}

  @OnMessage
  public void handleMessage(String message) {
    WebSocketConfig.getInputAdapter().appendMessage(message);
  }

//  public Session getSession() {
//    return session;
//  }
}
