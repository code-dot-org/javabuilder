package org.code.protocol;

import org.json.JSONException;
import org.json.JSONObject;

public class FormattedClientMessage extends ClientMessage {

  private FormattedClientMessage(ClientMessageType type, String value, JSONObject detail) {
    super(type, value, detail);
  }

  /**
   * Create a client message from a formatted message. This only supports Neighborhood messages now as this exists
   * specifically as a short-term hack to unblock neighborhood level writing.
   * @deprecated
   * This method is only here as a workaround until we have designed a more formal method for client-facing communication
   * from sub-projects of javabuilder.
   * @param formattedMessage a message created by ClientMessage.getFormattedMessage
   */
  public static FormattedClientMessage buildClientMessage(String formattedMessage) {
    JSONObject message;
    try {
      message = new JSONObject(formattedMessage);
      String type = message.getString("type");
      if (!type.equals(ClientMessageType.NEIGHBORHOOD.toString())) {
        // We only support neighborhood messages now.
        return null;
      }
      String value = message.getString("value");
      JSONObject detail = null;
      if (message.has("detail")) {
        // A message doesn't need a detail object.
        detail = message.getJSONObject("detail");
      }
      return new FormattedClientMessage(ClientMessageType.NEIGHBORHOOD, value, detail);
    } catch (JSONException e) {
      // This is not a client message. return null
      return null;
    }
  }
}
