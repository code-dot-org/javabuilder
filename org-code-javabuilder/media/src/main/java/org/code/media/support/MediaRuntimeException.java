package org.code.media.support;

import org.code.protocol.JavabuilderRuntimeException;

public class MediaRuntimeException extends JavabuilderRuntimeException {
  public MediaRuntimeException(MediaRuntimeExceptionKeys key) {
    super(key);
  }

  public MediaRuntimeException(MediaRuntimeExceptionKeys key, Throwable cause) {
    super(key, cause);
  }
}
