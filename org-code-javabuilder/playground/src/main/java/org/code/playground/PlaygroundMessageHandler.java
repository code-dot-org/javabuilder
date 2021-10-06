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
  // A websocket request can be up to 128 kb, which is around 131,000 characters.
  // Make sure we don't get close to this limit.
  private final int MAX_CHARACTERS_PER_MESSAGE = 120000;

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

    int currentLength = 0;
    while (this.queuedMessages.peek() != null) {
      PlaygroundMessage message = this.queuedMessages.remove();
      String messageStr = message.getFormattedMessage();
      // If we have existing messages and this message would put us over our limit, send existing
      // messages.
      // Note: we could still potentially send a too large message if a single message was too
      // large.
      // This is extremely unlikely as it would require either a filename or text item equivalent
      // to more than 30 pages of text. In this case it's fine to attempt the message and fail, as
      // this
      // is bad user behavior.
      if (currentLength > 0 && currentLength + messageStr.length() > MAX_CHARACTERS_PER_MESSAGE) {
        this.sendBatchedMessageHelper(messages);
        currentLength = 0;
        messages = new JSONArray();
      }
      messages.put(new JSONObject(message.getFormattedMessage()));
      currentLength += messageStr.length();
    }
    this.sendBatchedMessageHelper(messages);
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

  private void sendBatchedMessageHelper(JSONArray messagesToSend) {
    JSONObject messageObject = new JSONObject();
    messageObject.put(ClientMessageDetailKeys.UPDATES, messagesToSend);
    this.outputAdapter.sendMessage(
        new PlaygroundMessage(PlaygroundSignalKey.UPDATE, messageObject));
  }
}
