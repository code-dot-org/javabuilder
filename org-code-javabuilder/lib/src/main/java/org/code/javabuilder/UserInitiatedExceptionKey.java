package org.code.javabuilder;

/** These keys describe server-side errors caused by the user's code. */
public enum UserInitiatedExceptionKey {
  // The user's code tried to access a method that it is not allowed to access.
  ILLEGAL_METHOD_ACCESS,
  // The user's code hit an error while it was running.
  RUNTIME_ERROR,
  // The user's code has more than one main method.
  TWO_MAIN_METHODS,
  // The user's code does not contain a main method.
  NO_MAIN_METHOD,
  // The user's code has a compiler error.
  COMPILER_ERROR,
  // The user tried to include a source file that did not end in .java
  JAVA_EXTENSION_MISSING,
  // The user is writing to file too many times
  TOO_MANY_WRITES
}
