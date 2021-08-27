package org.code.protocol;

/** These keys map to client-side strings that are translatable. */
public enum InternalErrorKey {
  // We caused an error while executing the user's program.
  INTERNAL_RUNTIME_EXCEPTION,
  // We caused an error while compiling the user's program.
  INTERNAL_COMPILER_EXCEPTION,
  // We caused an error.
  INTERNAL_EXCEPTION,
  // We caused an error that we aren't currently tracking.
  UNKNOWN_ERROR
}
