package org.code.protocol;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

/* Helper for formatting exceptions that will be displayed to the user. */
public final class JavabuilderThrowableMessageUtils {
  private JavabuilderThrowableMessageUtils() {
    throw new UnsupportedOperationException("Instantiation of utility class is not allowed.");
  }

  public static JavabuilderThrowableMessage getExceptionMessage(
      Throwable throwable, Enum key, String fallbackMessage) {
    HashMap<String, String> detail = new HashMap<>();
    detail.put(ClientMessageDetailKeys.CONNECTION_ID, Properties.getConnectionId());
    if (throwable.getCause() != null) {
      detail.put(ClientMessageDetailKeys.CAUSE, getLoggingString(throwable));
      if (throwable.getCause().getMessage() != null) {
        detail.put(ClientMessageDetailKeys.CAUSE_MESSAGE, throwable.getCause().getMessage());
      }
    }

    if (fallbackMessage != null) {
      detail.put(ClientMessageDetailKeys.FALLBACK_MESSAGE, fallbackMessage);
    }

    return new JavabuilderThrowableMessage(key, detail);
  }

  /** @return A pretty version of the exception and stack trace. */
  public static String getLoggingString(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    throwable.printStackTrace(printWriter);
    return stringWriter.toString();
  }
}
