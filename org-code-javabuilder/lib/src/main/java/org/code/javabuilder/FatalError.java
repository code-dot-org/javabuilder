package org.code.javabuilder;

import org.code.protocol.JavabuilderError;

/**
 * A non-recoverable error that affects the user. This error typically results in shutting down the
 * runtime.
 */
public class FatalError extends JavabuilderError {
  private final int errorCode;

  public FatalError(FatalErrorKey key) {
    super(key);
    this.errorCode = key.getErrorCode();
  }

  public FatalError(FatalErrorKey key, Throwable cause) {
    super(key, cause);
    this.errorCode = key.getErrorCode();
  }

  public int getErrorCode() {
    return this.errorCode;
  }
}
