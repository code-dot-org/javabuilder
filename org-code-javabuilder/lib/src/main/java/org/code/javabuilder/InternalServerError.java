package org.code.javabuilder;

import org.code.protocol.InternalErrorKey;
import org.code.protocol.JavabuilderException;

/**
 * An exception that we caused that affects the user. These are the conceptual equivalent of HTTP
 * 500 errors.
 */
public class InternalServerError extends JavabuilderException {
  public InternalServerError(InternalErrorKey key) {
    super(key);
  }

  public InternalServerError(InternalErrorKey key, Throwable cause) {
    super(key, cause);
  }
}
