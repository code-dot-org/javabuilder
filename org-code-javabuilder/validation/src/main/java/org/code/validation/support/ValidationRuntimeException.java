package org.code.validation.support;

import org.code.protocol.JavabuilderRuntimeException;

public class ValidationRuntimeException extends JavabuilderRuntimeException {
  protected ValidationRuntimeException(ExceptionKey key) {
    super(key);
    this.setFallbackMessage(this.generateFallbackMessage(key, null));
  }

  protected ValidationRuntimeException(ExceptionKey key, Throwable cause) {
    super(key, cause);
    this.setFallbackMessage(this.generateFallbackMessage(key, cause));
  }

  private String generateFallbackMessage(ExceptionKey key, Throwable cause) {
    switch (key) {
      case NO_MAIN_METHOD:
        return "This test requires a main method. No main method was found.";
      case ERROR_RUNNING_MAIN:
        // TODO: better error messaging when we catch one of our custom errors (such as
        // TheaterRuntimeException).
        String message = "We hit an exception running your main method.";
        if (cause != null) {
          String causeName = cause.getClass().getSimpleName();
          if (causeName.length() > 0) {
            message = String.format("A %s was thrown in your main method.", causeName);
          }
        }
        return message;
      default:
        return "";
    }
  }
}
