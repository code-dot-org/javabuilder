package org.code.playground;

import java.util.LinkedList;
import java.util.Queue;
import org.code.protocol.ClientMessageDetailKeys;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.MessageHandler;
import org.code.protocol.OutputAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

class PlaygroundMessageHandler implements MessageHandler {
  private static PlaygroundMessageHandler instance;
  private boolean messagesEnabled;
  private final Queue<PlaygroundMessage> queuedMessages;

  static PlaygroundMessageHandler getInstance() {
    if (instance == null) {
      instance = new PlaygroundMessageHandler();
    }
    return PlaygroundMessageHandler.instance;
  }

  private final OutputAdapter outputAdapter;

  private PlaygroundMessageHandler() {
    this(GlobalProtocol.getInstance().getOutputAdapter());
    GlobalProtocol.getInstance().registerMessageHandler(this);
  }

  // Visible for testing only
  PlaygroundMessageHandler(OutputAdapter outputAdapter) {
    this.messagesEnabled = true;
    this.outputAdapter = outputAdapter;
    this.queuedMessages = new LinkedList<>();
  }

  public void sendMessage(PlaygroundMessage message) {
    // only send messages if playground has not ended. Otherwise throw a runtime exception.
    if (!this.messagesEnabled) {
      throw new PlaygroundRuntimeException(PlaygroundExceptionKeys.INVALID_MESSAGE);
    } else {
      this.queuedMessages.add(message);
    }
  }

  public void sendBatchedMessages() {
    if (this.queuedMessages.isEmpty()) {
      return;
    }
    JSONArray messages = new JSONArray();
    // copy existing queue so any new messages that come in during parsing
    // will be handled in next batch
    for (PlaygroundMessage message : this.queuedMessages) {
      messages.put(new JSONObject(message.getFormattedMessage()));
    }
    this.queuedMessages.clear();
    JSONObject messageObject = new JSONObject();
    messageObject.put(ClientMessageDetailKeys.UPDATES, messages);
    this.outputAdapter.sendMessage(
        new PlaygroundMessage(PlaygroundSignalKey.UPDATE, messageObject));
  }

  @Override
  public void exit() {
    this.sendBatchedMessages();
  }

  @Override
  public void enableMessages() {
    this.messagesEnabled = true;
  }

  @Override
  public void disableMessages() {
    this.messagesEnabled = false;
  }
}
