package org.code.playground;

import org.code.protocol.JavabuilderException;

public class PlaygroundException extends JavabuilderException {
  protected PlaygroundException(PlaygroundExceptionKeys key) {
    super(key);
  }
}
