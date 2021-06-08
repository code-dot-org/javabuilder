package org.code.protocol;

public class InternalJavabuilderError extends JavabuilderRuntimeException{
  public InternalJavabuilderError(InternalErrorKey key) {
    super(key);
  }

  public InternalJavabuilderError(InternalErrorKey key, Throwable cause) {
    super(key, cause);
  }
}
