package org.code.javabuilder;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.code.protocol.LoggerConstants;
import org.json.JSONException;
import org.json.JSONObject;

public class LambdaLogHandler extends Handler {
  private final LambdaLogger logger;
  private final String sessionId;
  private final String connectionId;
  private final String levelId;
  private final String channelId;

  public LambdaLogHandler(
      LambdaLogger logger,
      String sessionId,
      String connectionId,
      String levelId,
      String channelId) {
    this.logger = logger;
    this.sessionId = sessionId;
    this.connectionId = connectionId;
    this.levelId = levelId;
    this.channelId = channelId;
  }

  @Override
  public void publish(LogRecord record) {
    JSONObject sessionMetadata = new JSONObject();
    sessionMetadata.put(LoggerConstants.SESSION_ID, this.sessionId);
    sessionMetadata.put(LoggerConstants.CONNECTION_ID, this.connectionId);
    sessionMetadata.put(LoggerConstants.LEVEL_ID, this.levelId);
    sessionMetadata.put(LoggerConstants.CHANNEL_ID, this.channelId);

    JSONObject logData = new JSONObject();
    logData.put(LoggerConstants.SESSION_METADATA, sessionMetadata);
    String message = record.getMessage();
    // try to send message as json if possible.
    try {
      JSONObject jsonMessage = new JSONObject(message);
      logData.put(LoggerConstants.MESSAGE, jsonMessage);
    } catch (JSONException e) {
      logData.put(LoggerConstants.MESSAGE, message);
    }
    logData.put(LoggerConstants.LEVEL, record.getLevel());

    this.logger.log(logData.toString());
  }

  @Override
  public void flush() {}

  @Override
  public void close() {}
}
