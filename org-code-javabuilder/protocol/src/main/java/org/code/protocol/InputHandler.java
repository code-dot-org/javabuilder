package org.code.protocol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * [PROTOTYPE CODE]
 *
 * <p>Handles retrieving various types of JSON messages from the client. Currently expects JSON in
 * the format:
 *
 * <p>{ "messageType": "<message type>", "message": "<message contents>" }
 */
public class InputHandler {
  private final Map<InputMessageType, Queue<String>> inputQueues;
  private final InputAdapter inputAdapter;

  public InputHandler(InputAdapter inputAdapter) {
    this.inputAdapter = inputAdapter;
    this.inputQueues = new HashMap<>();
  }

  public String getNextMessageForType(InputMessageType type) {
    if (inputQueues.containsKey(type) && inputQueues.get(type).peek() != null) {
      return inputQueues.get(type).remove();
    }

    InputMessageType nextMessageType = null;
    while (nextMessageType != type) {
      final String nextMessage = this.inputAdapter.getNextMessage();
      final Map<String, Object> parsedMessage = this.parseMessage(nextMessage);
      if (parsedMessage == null) {
        continue;
      }

      nextMessageType = InputMessageType.valueOf((String) parsedMessage.get("messageType"));
      final String message = (String) parsedMessage.get("message");

      if (!inputQueues.containsKey(nextMessageType)) {
        inputQueues.put(nextMessageType, new LinkedList<>());
      }

      inputQueues.get(nextMessageType).add(message);
    }

    return inputQueues.get(type).remove();
  }

  private Map<String, Object> parseMessage(String message) {
    try {
      return new JSONObject(message).toMap();
    } catch (JSONException e) {
      // TODO handle error - swallowing for now
      System.out.println("Error parsing JSON " + e);
      return null;
    }
  }
}
