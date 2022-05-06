package org.code.javabuilder;

import org.code.protocol.InternalExceptionKey;
import org.code.protocol.JavabuilderException;

/**
 * An exception that we caused that affects the user. These are the conceptual equivalent of HTTP
 * 500 errors.
 */
public class InternalServerException extends JavabuilderException {
  public InternalServerException(InternalExceptionKey key) {
    super(key);
  }

  public InternalServerException(InternalExceptionKey key, Throwable cause) {
    super(key, cause);
  }
}
