package org.code.protocol;

import java.util.HashMap;

/** Parent error for all errors that will be displayed to the user. */
public abstract class JavabuilderRuntimeException extends RuntimeException
    implements JavabuilderThrowableProtocol {
  private final Enum key;
  private String fallbackMessage;

  protected JavabuilderRuntimeException(Enum key) {
    super(key.toString());
    this.key = key;
  }

  protected JavabuilderRuntimeException(Enum key, Throwable cause) {
    super(key.toString(), cause);
    this.key = key;
  }

  protected JavabuilderRuntimeException(Enum key, String fallbackMessage) {
    super(key.toString());
    this.key = key;
    this.fallbackMessage = fallbackMessage;
  }

  public JavabuilderThrowableMessage getExceptionMessage() {
    return JavabuilderThrowableMessageUtils.getExceptionMessage(
        this, this.key, this.fallbackMessage);
  }

  public HashMap<String, String> getExceptionDetails() {
    return JavabuilderThrowableMessageUtils.getExceptionDetails(this, this.fallbackMessage);
  }

  public String getLoggingString() {
    return JavabuilderThrowableMessageUtils.getLoggingString(this);
  }

  public String getFallbackMessage() {
    return this.fallbackMessage;
  }

  protected void setFallbackMessage(String fallbackMessage) {
    this.fallbackMessage = fallbackMessage;
  }
}
