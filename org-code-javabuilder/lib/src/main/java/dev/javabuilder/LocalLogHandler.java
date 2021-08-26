package dev.javabuilder;

import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
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
    logData.put("message", record.getMessage());
    logData.put("level", record.getLevel());

    this.logStream.println(logData.toString());
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
