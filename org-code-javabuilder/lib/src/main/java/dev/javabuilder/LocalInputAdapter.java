package dev.javabuilder;

import java.util.LinkedList;
import java.util.Queue;
import org.code.javabuilder.InputAdapter;

/** Intended for local testing only. Mimics the input we would expect to get from AWS SQS */
public class LocalInputAdapter implements InputAdapter {
  private final Queue<String> messages;

  public LocalInputAdapter() {
    this.messages = new LinkedList<>();
    messages.add("one\n");
    messages.add("two\n");
    messages.add("three\n");
  }

  @Override
  public String getNextMessage() {
    if (messages.peek() == null) {
      return null;
    }
    return messages.remove();
  }
}
