package org.code.protocol;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
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

    Throwable cause = throwable.getCause();
    if (cause != null) {
      detail.put(ClientMessageDetailKeys.CAUSE, getUserFacingLoggingString(cause));
      if (cause.getMessage() != null) {
        detail.put(ClientMessageDetailKeys.CAUSE_MESSAGE, cause.getMessage());
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

  /**
   * @return A pretty shortened version of the exception and stack trace for sharing with end users.
   *     Private, as this is meant for use as a helper in the context of a client-facing message.
   */
  private static String getUserFacingLoggingString(Throwable cause) {
    if (cause.getClass() == InvocationTargetException.class) {
      cause = cause.getCause();
    }
    StackTraceElement[] stackTrace = cause.getStackTrace();
    String loggingString = cause.toString() + "\n";

    for (StackTraceElement stackTraceElement : stackTrace) {
      if (stackTraceElement.isNativeMethod()) {
        break;
      }
      loggingString += "\t" + stackTraceElement.toString() + "\n";
    }
    return loggingString;
  }
}
