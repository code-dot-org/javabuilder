package org.code.javabuilder;

import org.code.protocol.JavabuilderThrowableMessageUtils;
import org.code.protocol.LoggableProtocol;

public class InternalFacingRuntimeException extends RuntimeException implements LoggableProtocol {
  public InternalFacingRuntimeException(String errorMessage, Throwable cause) {
    super(errorMessage, cause);
  }

  /** @return A pretty version of the exception and stack trace. */
  public String getLoggingString() {
    return JavabuilderThrowableMessageUtils.getLoggingString(this);
  }
}
