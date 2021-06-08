package org.code.javabuilder;

import org.code.protocol.InternalErrorKey;
import org.code.protocol.JavabuilderException;

/**
 * An exception caused by us that is intended to be seen by the user. These are the conceptual
 * equivalent of HTTP 500 errors.
 */
public class UserFacingException extends JavabuilderException {
  public UserFacingException(InternalErrorKey key) {
    super(key);
  }

  public UserFacingException(InternalErrorKey key, Throwable cause) {
    super(key, cause);
  }
}
