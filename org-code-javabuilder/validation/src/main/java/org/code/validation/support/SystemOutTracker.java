package org.code.validation.support;

import java.util.ArrayList;
import java.util.List;
import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;

public class SystemOutTracker {
  private List<String> messages;

  public SystemOutTracker() {
    this.messages = new ArrayList<>();
  }

  public List<String> getSystemOutMessages() {
    return this.messages;
  }

  // Track a single client message event. It will only be tracked if it is a system out message
  // that is not just a newline, otherwise it will be ignored.
  public void trackEvent(ClientMessage message) {
    if (message.getType() != ClientMessageType.SYSTEM_OUT) {
      return;
    }
    String value = message.getValue();
    // Ignore new lines as those are indications of a println vs a print.
    // For validation we don't care which is used.
    if (!value.equals("\n")) {
      this.messages.add(message.getValue());
    }
  }

  public void reset() {
    this.messages = new ArrayList<>();
  }
}
