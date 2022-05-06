package org.code.neighborhood.support;

import org.code.protocol.JavabuilderRuntimeException;

public class NeighborhoodRuntimeException extends JavabuilderRuntimeException {
  public NeighborhoodRuntimeException(ExceptionKeys key) {
    super(key);
  }

  public NeighborhoodRuntimeException(ExceptionKeys key, Throwable cause) {
    super(key, cause);
  }
}
