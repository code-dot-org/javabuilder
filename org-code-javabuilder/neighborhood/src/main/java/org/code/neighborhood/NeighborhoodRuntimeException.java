package org.code.neighborhood;

import org.code.protocol.JavabuilderRuntimeException;

public class NeighborhoodRuntimeException extends JavabuilderRuntimeException {
  protected NeighborhoodRuntimeException(ExceptionKeys key) {
    super(key);
  }
  protected NeighborhoodRuntimeException(ExceptionKeys key, Throwable cause) {
    super(key, cause);
  }
}
