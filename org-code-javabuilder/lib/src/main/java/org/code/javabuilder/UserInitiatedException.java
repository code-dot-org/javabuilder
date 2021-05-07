package org.code.javabuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

/** An exception caused by a user action. */
public class UserInitiatedException extends Exception {
  private final UserInitiatedExceptionKey key;

  public UserInitiatedException(UserInitiatedExceptionKey key) {
    super(key.toString());
    this.key = key;
  }

  public UserInitiatedException(UserInitiatedExceptionKey key, Exception cause) {
    super(key.toString(), cause);
    this.key = key;
  }

  public UserInitiatedExceptionMessage getExceptionMessage() {
    HashMap<String, String> detail = new HashMap<>();
    if (getCause() != null) {
      detail.put("cause", getCause().getMessage());
    }
    if (getMessage() != null) {
      detail.put("message", getMessage());
    }

    return new UserInitiatedExceptionMessage(this.key, detail.size() > 0 ? detail : null);
  }

  /** @return A pretty version of the exception and stack trace. */
  public String getLoggingString() {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    printStackTrace(printWriter);
    return stringWriter.toString();
  }
}
