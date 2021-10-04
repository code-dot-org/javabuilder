package org.code.playground;

import org.code.protocol.GlobalProtocol;
import org.code.protocol.OutputAdapter;

class PlaygroundMessageHandler {
  private static PlaygroundMessageHandler instance;
  private boolean messagesEnabled;

  static PlaygroundMessageHandler getInstance() {
    if (instance == null) {
      instance = new PlaygroundMessageHandler();
    }
    return PlaygroundMessageHandler.instance;
  }

  private final OutputAdapter outputAdapter;

  private PlaygroundMessageHandler() {
    this(GlobalProtocol.getInstance().getOutputAdapter());
  }

  // Visible for testing only
  PlaygroundMessageHandler(OutputAdapter outputAdapter) {
    this.messagesEnabled = true;
    this.outputAdapter = outputAdapter;
  }

  public void sendMessage(PlaygroundMessage message) {
    // only send messages if playground has not ended. Otherwise throw a runtime exception.
    if (!this.messagesEnabled) {
      throw new PlaygroundRuntimeException(PlaygroundExceptionKeys.INVALID_MESSAGE);
    } else {
      this.outputAdapter.sendMessage(message);
    }
  }

  protected void enableMessages() {
    this.messagesEnabled = true;
  }

  protected void disableMessages() {
    this.messagesEnabled = false;
  }
}
