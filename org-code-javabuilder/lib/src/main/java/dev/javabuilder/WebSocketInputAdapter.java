package dev.javabuilder;

import org.code.javabuilder.InputAdapter;

import java.util.LinkedList;
import java.util.Queue;

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
    messages.add(message);
  }
}
