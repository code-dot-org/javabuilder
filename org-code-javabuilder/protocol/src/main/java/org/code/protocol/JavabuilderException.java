package org.code.protocol;

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
    return JavabuilderThrowableMessageHelper.getExceptionMessage(
        this, this.key, this.fallbackMessage);
  }

  /** @return A pretty version of the exception and stack trace. */
  public String getLoggingString() {
    return JavabuilderThrowableMessageHelper.getLoggingString(this);
  }
}
