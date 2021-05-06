package org.code.javabuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

/** Exception with a message intended to be seen by the user */
public class UserFacingException extends Exception {
  private final UserFacingExceptionKey key;

  public UserFacingException(UserFacingExceptionKey key) {
    super(key.toString());
    this.key = key;
  }

  public UserFacingException(UserFacingExceptionKey key, Exception cause) {
    super(key.toString(), cause);
    this.key = key;
  }

  public UserFacingExceptionMessage getExceptionMessage() {
    HashMap<String, String> detail = null;
    if (getCause() != null) {
      detail = new HashMap<>();
      detail.put("cause", getCause().getMessage());
    }
    return new UserFacingExceptionMessage(this.key, detail);
  }

  /** @return A pretty version of the exception and stack trace. */
  public String getLoggingString() {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    printStackTrace(printWriter);
    return stringWriter.toString();
  }
}
