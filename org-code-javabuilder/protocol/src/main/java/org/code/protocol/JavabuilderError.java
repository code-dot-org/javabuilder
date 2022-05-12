package org.code.protocol;

/** Parent error for all errors that are displayed to the user. */
public abstract class JavabuilderError extends Error implements JavabuilderThrowableProtocol {
  private final Enum key;
  private String fallbackMessage;

  protected JavabuilderError(Enum key) {
    super(key.toString());
    this.key = key;
  }

  protected JavabuilderError(Enum key, Throwable cause) {
    super(key.toString(), cause);
    this.key = key;
  }

  protected JavabuilderError(Enum key, String fallbackMessage) {
    super(key.toString());
    this.key = key;
    this.fallbackMessage = fallbackMessage;
  }

  public JavabuilderThrowableMessage getExceptionMessage() {
    return JavabuilderThrowableMessageUtils.getExceptionMessage(
        this, this.key, this.fallbackMessage);
  }

  /** @return A pretty version of the exception and stack trace. */
  public String getLoggingString() {
    return JavabuilderThrowableMessageUtils.getLoggingString(this);
  }
}
