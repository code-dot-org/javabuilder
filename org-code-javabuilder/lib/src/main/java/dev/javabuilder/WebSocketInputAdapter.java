package dev.javabuilder;

import java.util.LinkedList;
import java.util.Queue;
import org.code.protocol.InputAdapter;

/** Intended for local testing with dashboard only. Accepts input from a WebSocket session. */
public class WebSocketInputAdapter implements InputAdapter {
  private final Queue<String> messages;

  public WebSocketInputAdapter() {
    this.messages = new LinkedList<>();
  }

  @Override
  public String getNextMessage() {
    while (messages.peek() == null) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return messages.remove();
  }

  public void appendMessage(String message) {
    messages.add(message + System.lineSeparator());
  }
}
