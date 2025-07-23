package org.code.theater.support;

import org.code.protocol.JavabuilderRuntimeException;

public class TheaterRuntimeException extends JavabuilderRuntimeException {
  public TheaterRuntimeException(ExceptionKeys key) {
    super(key);
  }

  public TheaterRuntimeException(ExceptionKeys key, Throwable cause) {
    super(key, cause);
  }

  protected TheaterRuntimeException(ExceptionKeys key, String fallbackMessage) {
    super(key, fallbackMessage);
  }
}
