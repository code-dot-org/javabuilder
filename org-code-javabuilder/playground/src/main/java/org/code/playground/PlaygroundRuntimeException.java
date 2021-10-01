package org.code.playground;

import org.code.protocol.JavabuilderRuntimeException;

public class PlaygroundRuntimeException extends JavabuilderRuntimeException {
  protected PlaygroundRuntimeException(PlaygroundExceptionKeys key) {
    super(key);
  }

  protected PlaygroundRuntimeException(PlaygroundExceptionKeys key, Throwable cause) {
    super(key, cause);
  }
}
