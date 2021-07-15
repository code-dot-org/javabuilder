package org.code.javabuilder;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.code.protocol.JavabuilderLogger;
import org.json.JSONObject;

public class LambdaLogHandler implements JavabuilderLogger {
  private final String sessionId;
  private final LambdaLogger logger;

  public LambdaLogHandler(LambdaLogger logger, String sessionId) {
    this.logger = logger;
    this.sessionId = sessionId;
  }

  @Override
  public void logInfo(JSONObject eventData) {
    this.logger.log("INFO: " + eventData.toString());
  }

  @Override
  public void logError(JSONObject eventData) {
    eventData.put("sessionId", this.sessionId);
    this.logger.log("ERROR: " + eventData.toString());
  }
}
