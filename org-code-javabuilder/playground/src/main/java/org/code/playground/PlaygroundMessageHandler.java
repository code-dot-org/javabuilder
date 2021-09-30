package org.code.playground;

import org.code.protocol.GlobalProtocol;
import org.code.protocol.OutputAdapter;

class PlaygroundMessageHandler {
  private static PlaygroundMessageHandler instance;

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
    this.outputAdapter = outputAdapter;
  }

  public void sendMessage(PlaygroundMessage message) {
    // only send messages if playground has not ended. Otherwise throw a runtime exception.
    if (Playground.board.hasEnded()) {
      throw new PlaygroundRuntimeException(
          PlaygroundExceptionKeys.INVALID_MESSAGE_PLAYGROUND_ENDED);
    } else {
      this.outputAdapter.sendMessage(message);
    }
  }
}
