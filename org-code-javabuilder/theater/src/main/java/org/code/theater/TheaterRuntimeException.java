package org.code.theater;

import org.code.protocol.JavabuilderRuntimeException;

public class TheaterRuntimeException extends JavabuilderRuntimeException {
  protected TheaterRuntimeException(ExceptionKeys key) {
    super(key);
  }

  protected TheaterRuntimeException(ExceptionKeys key, Throwable cause) {
    super(key, cause);
  }

  protected TheaterRuntimeException(ExceptionKeys key, String fallbackMessage) {
    super(key, fallbackMessage);
  }
}
