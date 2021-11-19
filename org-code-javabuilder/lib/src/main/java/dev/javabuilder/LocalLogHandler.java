package dev.javabuilder;

import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.json.JSONException;
import org.json.JSONObject;

public class LocalLogHandler extends Handler {
  private final PrintStream logStream;
  private final String levelId;
  private final String channelId;

  public LocalLogHandler(PrintStream logStream, String levelId, String channelId) {
    this.logStream = logStream;
    this.levelId = levelId;
    this.channelId = channelId;
  }

  @Override
  public void publish(LogRecord record) {
    JSONObject sessionMetadata = new JSONObject();
    sessionMetadata.put("levelId", this.levelId);
    sessionMetadata.put("channelId", this.channelId);

    JSONObject logData = new JSONObject();
    logData.put("sessionMetadata", sessionMetadata);

    String message = record.getMessage();
    try {
      JSONObject jsonMessage = new JSONObject(message);
      logData.put("message", jsonMessage);
    } catch (JSONException e) {
      logData.put("message", message);
    }
    logData.put("level", record.getLevel());

    this.logStream.println(logData);
  }

  @Override
  public void flush() {
    logStream.flush();
  }

  @Override
  public void close() {
    logStream.close();
  }
}
