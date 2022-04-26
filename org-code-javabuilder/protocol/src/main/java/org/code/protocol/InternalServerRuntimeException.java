package org.code.protocol;

/**
 * An exception that we caused from within a student-facing API (such as Neighborhood) that affects
 * the user. These are RuntimeExceptions so a student will not get compiler errors for failing to
 * handle them. These are the conceptual equivalent of HTTP 500 errors.
 */
public class InternalServerRuntimeException extends JavabuilderRuntimeException {
  public InternalServerRuntimeException(InternalExceptionKey key) {
    super(key);
  }

  public InternalServerRuntimeException(InternalExceptionKey key, Throwable cause) {
    super(key, cause);
  }
}
