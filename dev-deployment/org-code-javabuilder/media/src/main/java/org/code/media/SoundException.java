package org.code.media;

import org.code.media.support.SoundExceptionKeys;
import org.code.protocol.JavabuilderRuntimeException;

public class SoundException extends JavabuilderRuntimeException {
  public SoundException(SoundExceptionKeys key) {
    super(key);
  }

  public SoundException(SoundExceptionKeys key, Throwable cause) {
    super(key, cause);
  }
}
