package org.code.media;

import org.code.protocol.JavabuilderRuntimeException;

public class SoundException extends JavabuilderRuntimeException {
  protected SoundException(SoundExceptionKeys key) {
    super(key);
  }

  protected SoundException(SoundExceptionKeys key, Throwable cause) {
    super(key, cause);
  }
}
