package org.code.javabuilder;

import java.io.PrintWriter;
import java.io.StringWriter;

/** Exception caused by a user action. */
public class UserInitiatedException extends Exception {
  public UserInitiatedException(String errorMessage) {
    super(errorMessage);
  }

  public UserInitiatedException(String errorMessage, Exception cause) {
    super(errorMessage, cause);
  }

  /** @return A pretty version of the exception and stack trace. */
  public String getLoggingString() {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    printStackTrace(printWriter);
    return stringWriter.toString();
  }
}
