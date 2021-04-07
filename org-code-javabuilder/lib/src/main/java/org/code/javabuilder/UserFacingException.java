package org.code.javabuilder;

import java.io.PrintWriter;
import java.io.StringWriter;

/** Exception with a message intended to be seen by the user */
public class UserFacingException extends Exception {
  public UserFacingException(String errorMessage) {
    super(errorMessage);
  }

  public UserFacingException(String errorMessage, Exception cause) {
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
