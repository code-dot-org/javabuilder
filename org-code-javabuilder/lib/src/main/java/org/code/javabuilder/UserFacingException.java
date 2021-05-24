package org.code.javabuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

/**
 * An exception caused by us that is intended to be seen by the user. These are the conceptual
 * equivalent of HTTP 500 errors.
 */
public class UserFacingException extends Exception {
  private final UserFacingExceptionKey key;

  public UserFacingException(UserFacingExceptionKey key) {
    super(key.toString());
    this.key = key;
  }

  public UserFacingException(UserFacingExceptionKey key, Throwable cause) {
    super(key.toString(), cause);
    this.key = key;
  }

  public UserFacingExceptionMessage getExceptionMessage() {
    HashMap<String, String> detail = new HashMap<>();
    detail.put("connectionId", Properties.getConnectionId());
    if (this.getCause() != null) {
      detail.put("cause", this.getLoggingString());
    }
    return new UserFacingExceptionMessage(this.key, detail);
  }

  /** @return A pretty version of the exception and stack trace. */
  public String getLoggingString() {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    this.printStackTrace(printWriter);
    return stringWriter.toString();
  }
}
