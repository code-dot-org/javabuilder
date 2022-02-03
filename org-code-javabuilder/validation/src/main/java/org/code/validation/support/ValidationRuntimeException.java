package org.code.validation.support;

import org.code.protocol.JavabuilderRuntimeException;

public class ValidationRuntimeException extends JavabuilderRuntimeException {
  protected ValidationRuntimeException(ExceptionKey key) {
    super(key);
  }

  protected ValidationRuntimeException(ExceptionKey key, Throwable cause) {
    super(key, cause);
  }
}
