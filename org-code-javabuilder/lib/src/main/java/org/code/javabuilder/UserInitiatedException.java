package org.code.javabuilder;

import org.code.protocol.JavabuilderException;

/** An exception caused by a user action. */
public class UserInitiatedException extends JavabuilderException {
  public UserInitiatedException(UserInitiatedExceptionKey key) {
    super(key);
  }

  public UserInitiatedException(UserInitiatedExceptionKey key, Throwable cause) {
    super(key, cause);
  }
}
