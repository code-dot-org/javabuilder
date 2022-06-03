package org.code.javabuilder;

/**
 * Constants for internal facing exception types. Note that unlike other exception types, these do
 * not map to client-side strings because they are not sent to the user.
 */
public final class InternalFacingExceptionTypes {
  private InternalFacingExceptionTypes() {
    throw new UnsupportedOperationException("Instantiation of constants class not allowed");
  }

  public static final String CONNECTION_TERMINATED = "CONNECTION_TERMINATED";
  public static final String INVALID_INPUT = "INVALID_INPUT";
}
