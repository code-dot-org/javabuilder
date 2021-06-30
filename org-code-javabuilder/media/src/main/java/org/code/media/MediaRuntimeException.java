package org.code.media;

import org.code.protocol.JavabuilderRuntimeException;

public class MediaRuntimeException extends JavabuilderRuntimeException {
  protected MediaRuntimeException(MediaRuntimeExceptionKeys key) {
    super(key);
  }

  protected MediaRuntimeException(MediaRuntimeExceptionKeys key, Throwable cause) {
    super(key, cause);
  }
}
