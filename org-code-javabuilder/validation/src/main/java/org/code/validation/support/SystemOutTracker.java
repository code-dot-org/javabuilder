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

  public void trackEvent(ClientMessage message) {
    if (message.getType() != ClientMessageType.SYSTEM_OUT) {
      return;
    }
    this.messages.add(message.getValue());
  }
}
