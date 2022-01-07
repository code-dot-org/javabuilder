package org.code.protocol;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

/** Parent exception for all exceptions that will be displayed to the user. */
public abstract class JavabuilderException extends Exception
    implements JavabuilderThrowableProtocol {
  private final Enum key;
  private String fallbackMessage;

  protected JavabuilderException(Enum key) {
    super(key.toString());
    this.key = key;
  }

  protected JavabuilderException(Enum key, Throwable cause) {
    super(key.toString(), cause);
    this.key = key;
  }

  protected JavabuilderException(Enum key, String fallbackMessage) {
    super(key.toString());
    this.key = key;
    this.fallbackMessage = fallbackMessage;
  }

  public JavabuilderThrowableMessage getExceptionMessage() {
    HashMap<String, String> detail = new HashMap<>();
    detail.put(ClientMessageDetailKeys.CONNECTION_ID, Properties.getConnectionId());
    if (this.getCause() != null) {
      detail.put(ClientMessageDetailKeys.CAUSE, this.getLoggingString());
      if (this.getCause().getMessage() != null) {
        detail.put(ClientMessageDetailKeys.CAUSE_MESSAGE, this.getCause().getMessage());
      }
    }

    if (this.fallbackMessage != null) {
      detail.put(ClientMessageDetailKeys.FALLBACK_MESSAGE, this.fallbackMessage);
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
