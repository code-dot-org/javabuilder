package org.code.javabuilder;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Exception intended to eventually bubble up to a logger (i.e. CloudWatch) but have no functional
 * effect on the user. These should generally be used in code that executes after the user's code
 * has finished running.
 */
public class InternalFacingException extends Exception {
  public InternalFacingException(String errorMessage, Throwable cause) {
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
