package org.code.protocol;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.logging.Logger;
import org.json.JSONObject;

/** Parent error for all errors that will be displayed to the user. */
public abstract class JavabuilderRuntimeException extends RuntimeException
    implements JavabuilderThrowableProtocol {
  private final Enum key;

  protected JavabuilderRuntimeException(Enum key) {
    super(key.toString());
    this.key = key;
  }

  protected JavabuilderRuntimeException(Enum key, Throwable cause) {
    super(key.toString(), cause);
    this.key = key;
  }

  protected JavabuilderRuntimeException(Enum key, Boolean logOnCreate) {
    this(key);
    if (logOnCreate) {
      logError();
    }
  }

  protected JavabuilderRuntimeException(Enum key, Throwable cause, Boolean logOnCreate) {
    this(key, cause);
    if (logOnCreate) {
      logError();
    }
  }

  public JavabuilderThrowableMessage getExceptionMessage() {
    HashMap<String, String> detail = new HashMap<>();
    detail.put("connectionId", Properties.getConnectionId());
    if (this.getCause() != null) {
      detail.put("cause", this.getLoggingString());
    }

    return new JavabuilderThrowableMessage(this.key, detail);
  }

  /** @return A pretty version of the exception and stack trace. */
  public String getLoggingString() {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    this.printStackTrace(printWriter);
    return stringWriter.toString();
  }

  private void logError() {
    JSONObject eventData = new JSONObject();
    eventData.put("exceptionMessage", getExceptionMessage());
    eventData.put("loggingString", getLoggingString());
    Logger.getLogger(MAIN_LOGGER).severe(eventData.toString());
  }
}
