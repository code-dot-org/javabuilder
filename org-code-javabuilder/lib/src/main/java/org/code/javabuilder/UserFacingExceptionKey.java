package org.code.javabuilder;

/** These keys map to client-side strings that are translatable. */
public enum UserFacingExceptionKey {
  // We caused an error while executing the user's program.
  internalRuntimeException,
  // We caused an error while compiling the user's program.
  internalCompilerException,
  // We caused an error.
  internalException
}
