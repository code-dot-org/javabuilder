package org.code.protocol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handles retrieving various types of JSON messages from the client. Expects JSON in the format:
 *
 * <p>{ "messageType": "<message type>", "message": "<message contents>" }
 *
 * <p>Currently if JSON is not in this format, then it is assumed that the message is a System.in
 * message. TODO: This behavior only exists while Javalab has not been updated to use the new
 * messaging protocol. Remove this once Javalab is updated and published to production.
 */
public class InputHandler {
  private static final String MESSAGE_TYPE_KEY = "messageType";
  private static final String MESSAGE_KEY = "message";

  private final Map<InputMessageType, Queue<String>> inputQueues;
  private final InputAdapter inputAdapter;

  public InputHandler(InputAdapter inputAdapter) {
    this.inputAdapter = inputAdapter;
    this.inputQueues = new HashMap<>();
  }

  public String getNextMessageForType(InputMessageType type) {
    if (!inputQueues.containsKey(type) || inputQueues.get(type).peek() == null) {
      InputMessageType nextMessageType = null;
      while (nextMessageType != type) {
        final String nextMessageData = this.inputAdapter.getNextMessage();
        String message;

        try {
          final JSONObject jsonMessage = new JSONObject(nextMessageData);
          nextMessageType = InputMessageType.valueOf(jsonMessage.getString(MESSAGE_TYPE_KEY));
          message = jsonMessage.getString(MESSAGE_KEY);
        } catch (JSONException | IllegalArgumentException e) {
          // If the message is not JSON, then assume it is a System.in message.
          // TODO: Remove this and instead throw exception once Javalab is updated to send JSON
          nextMessageType = InputMessageType.SYSTEM_IN;
          message = nextMessageData;
        }

        if (!inputQueues.containsKey(nextMessageType)) {
          inputQueues.put(nextMessageType, new LinkedList<>());
        }

        inputQueues.get(nextMessageType).add(message);
      }
    }

    return inputQueues.get(type).remove();
  }
}
