package org.code.protocol;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/* Helper for formatting exceptions that will be displayed to the user. */
public final class JavabuilderThrowableMessageUtils {
  // The detail payload can get extremely large (eg, in case of sending stack trace from stack
  // overflow error).
  // We include two potentially very long detail keys (CAUSE and FALLBACK_MESSAGE) --
  // limit these to 50KB each, which should keep us well under 128KB payload size limit:
  // https://docs.aws.amazon.com/apigateway/latest/developerguide/limits.html
  private static final int MAX_DETAIL_SIZE_CHARS = 50000;
  // CAUSE_MESSAGE is used in Javalab to access to get relatively short strings
  // (eg, file names in FileNotFoundException, class names when using invalid classes)
  private static final int MAX_CAUSE_MESSAGE_DETAIL_SIZE_CHARS = 1000;

  private JavabuilderThrowableMessageUtils() {
    throw new UnsupportedOperationException("Instantiation of utility class is not allowed.");
  }

  public static JavabuilderThrowableMessage getExceptionMessage(
      Throwable throwable, Enum key, String fallbackMessage) {
    return new JavabuilderThrowableMessage(key, getExceptionDetails(throwable, fallbackMessage));
  }

  public static HashMap<String, String> getExceptionDetails(
      Throwable throwable, String fallbackMessage) {
    HashMap<String, String> detail = new HashMap<>();
    detail.put(ClientMessageDetailKeys.CONNECTION_ID, Properties.getConnectionId());

    // InvocationTargetException is thrown when there is an
    // exception thrown in code being executed via reflection (ie, student code).
    // We want to tell the user about that exception.
    Throwable cause = throwable.getCause();
    if (cause != null && cause instanceof InvocationTargetException) {
      cause = cause.getCause();
    }

    String preferredFallbackMessage = null;
    if (cause != null) {
      // To do: remove CAUSE key and replace with stack trace and exception message individually
      // to enable (partially) translated exception messages in Javalab.
      // https://codedotorg.atlassian.net/browse/JAVA-439
      preferredFallbackMessage = getDefaultFallbackMessageString(cause);
      detail.put(ClientMessageDetailKeys.CAUSE, preferredFallbackMessage);

      String causeMessage = cause.getMessage();
      if (causeMessage != null) {
        detail.put(
            ClientMessageDetailKeys.CAUSE_MESSAGE,
            trimDetail(causeMessage, MAX_CAUSE_MESSAGE_DETAIL_SIZE_CHARS));
      }
    }

    // Caller providing a fallback message overrides the default generated in this method.
    if (fallbackMessage != null) {
      preferredFallbackMessage = fallbackMessage;
    }
    if (preferredFallbackMessage != null) {
      detail.put(ClientMessageDetailKeys.FALLBACK_MESSAGE, preferredFallbackMessage);
    }
    return detail;
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
  private static String getDefaultFallbackMessageString(Throwable cause) {
    String loggingString = "Exception message: " + getExceptionMessageString(cause) + "\n";
    loggingString += getUserFacingStackTraceString(cause);
    return trimDetail(loggingString, MAX_DETAIL_SIZE_CHARS);
  }

  /**
   * Get the subset of the stack trace for the given cause that relates to user-specific code.
   *
   * @param cause
   * @return stack trace as a String
   */
  public static String getUserFacingStackTraceString(Throwable cause) {
    String stackTraceString = "";

    StackTraceElement[] stackTrace = cause.getStackTrace();
    for (StackTraceElement stackTraceElement : stackTrace) {
      if (stackTraceElement.isNativeMethod()) {
        break;
      }
      stackTraceString += "\t at " + stackTraceElement + "\n";
    }
    return stackTraceString;
  }

  private static String getExceptionMessageString(Throwable cause) {
    return cause.toString();
  }

  private static String trimDetail(String detailMessage, int charLimit) {
    String trimmedMessage = detailMessage;
    if (detailMessage.length() > charLimit) {
      trimmedMessage = trimmedMessage.substring(0, charLimit);
    }
    return trimmedMessage;
  }
}
