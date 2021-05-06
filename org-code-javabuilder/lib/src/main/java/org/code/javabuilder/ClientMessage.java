package org.code.javabuilder;

import java.util.HashMap;
import org.json.JSONObject;

public abstract class ClientMessage {
  private final ClientMessageType type;
  private final String value;
  private final HashMap<String, String> detail;
  //  private final JSONObject formattedMessage;
  //  private final JSONObject detail;
  ClientMessage(ClientMessageType type, String value, HashMap<String, String> detail) {
    this.type = type;
    this.value = value;
    this.detail = detail == null ? new HashMap<>() : detail;
    //    this.formattedMessage = new JSONObject();
    //    this.formattedMessage.put("type", type);
    //    this.formattedMessage.put("value", value);
    //    this.detail = new JSONObject(detail);
  }

  public void addDetail(String key, String value) {
    this.detail.put(key, value);
  }

  public ClientMessageType getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  public HashMap<String, String> getDetail() {
    return detail;
  }

  public String getFormattedMessage() {
    JSONObject formattedMessage = new JSONObject();
    formattedMessage.put("type", this.type);
    formattedMessage.put("value", this.value);
    if (this.detail.size() > 0) {
      formattedMessage.put("detail", this.detail);
    }
    return formattedMessage.toString();
  }
}
