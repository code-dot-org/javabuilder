package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

/**
 * Exception intended to eventually bubble up to a logger (i.e. CloudWatch) but have no functional
 * effect on the user. These should generally be used in code that executes after the user's code
 * has finished running.
 */
public class InternalFacingException extends Exception {
  public InternalFacingException(String errorMessage, Throwable cause) {
    super(errorMessage, cause);
    Logger.getLogger(MAIN_LOGGER).warning(this.getLoggingString());
  }

  /** @return A pretty version of the exception and stack trace. */
  public String getLoggingString() {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    printStackTrace(printWriter);
    return stringWriter.toString();
  }
}
