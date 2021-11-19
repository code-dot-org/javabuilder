package org.code.javabuilder;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
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
    sessionMetadata.put("sessionId", this.sessionId);
    sessionMetadata.put("connectionId", this.connectionId);
    sessionMetadata.put("levelId", this.levelId);
    sessionMetadata.put("channelId", this.channelId);

    JSONObject logData = new JSONObject();
    logData.put("sessionMetadata", sessionMetadata);
    String message = record.getMessage();
    // try to send message as json if possible.
    try {
      JSONObject jsonMessage = new JSONObject(message);
      logData.put("message", jsonMessage);
    } catch (JSONException e) {
      logData.put("message", message);
    }
    logData.put("level", record.getLevel());

    this.logger.log(logData.toString());
  }

  @Override
  public void flush() {}

  @Override
  public void close() {}
}
