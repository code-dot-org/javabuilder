package org.code.protocol;

import org.json.JSONObject;

public class JavabuilderLogger {
  private final LogHandler handler;
  private final String sessionId;
  private final String connectionId;
  private final String levelId;
  private final String channelId;

  public JavabuilderLogger(
      LogHandler handler, String sessionId, String connectionId, String levelId, String channelId) {
    this.handler = handler;
    this.sessionId = sessionId;
    this.connectionId = connectionId;
    this.levelId = levelId;
    this.channelId = channelId;
  }

  public void logInfo(JSONObject eventData) {
    this.log("INFO", eventData);
  }

  public void logError(JSONObject eventData) {
    this.log("ERROR", eventData);
  }

  private void log(String errorType, JSONObject eventData) {
    JSONObject sessionMetadata = new JSONObject();
    sessionMetadata.put("sessionId", this.sessionId);
    sessionMetadata.put("connectionId", this.connectionId);
    sessionMetadata.put("levelId", this.levelId);
    sessionMetadata.put("channelId", this.channelId);

    JSONObject logData = new JSONObject();
    logData.put("sessionMetadata", sessionMetadata);
    logData.put("eventData", eventData);

    this.handler.log(errorType + ": " + logData.toString());
  }
}
