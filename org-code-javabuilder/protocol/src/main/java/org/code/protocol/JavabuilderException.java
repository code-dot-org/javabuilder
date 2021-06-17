package org.code.protocol;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

/** Parent exception for all exceptions that will be displayed to the user. */
public abstract class JavabuilderException extends Exception
    implements JavabuilderThrowableProtocol {
  private final Enum key;

  protected JavabuilderException(Enum key) {
    super(key.toString());
    this.key = key;
  }

  protected JavabuilderException(Enum key, Throwable cause) {
    super(key.toString(), cause);
    this.key = key;
  }

  public JavabuilderThrowableMessage getExceptionMessage() {
    HashMap<String, String> detail = new HashMap<>();
    detail.put("connectionId", Properties.getConnectionId());
    if (this.getCause() != null) {
      detail.put("cause", this.getLoggingString());
    }

    return new JavabuilderThrowableMessage(this.key, detail);
  }

  /** @return A pretty version of the exception and stack trace. */
  public String getLoggingString() {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    this.printStackTrace(printWriter);
    return stringWriter.toString();
  }
}
