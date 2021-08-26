package org.code.javabuilder;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.code.protocol.LogHandler;

public class LambdaLogHandler implements LogHandler {
  private final LambdaLogger logger;

  public LambdaLogHandler(LambdaLogger logger) {
    this.logger = logger;
  }

  @Override
  public void log(String log) {
    this.logger.log(log);
  }

  //  @Override
  //  public void logInfo(JSONObject eventData) {
  //    this.logger.log("INFO: " + eventData.toString());
  //  }
  //
  //  @Override
  //  public void logError(JSONObject eventData) {
  //    eventData.put("sessionId", this.sessionId);
  //    this.logger.log("ERROR: " + eventData.toString());
  //  }
}
