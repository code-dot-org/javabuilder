package org.code.javabuilder;

/** These keys map to client-side keys that are translatable. Examples of what the strings */
public enum UserInitiatedExceptionKey {
  // The user's code tried to access a method that it is not allowed to access.
  illegalMethodAccess,
  // The user's code hit an error while it was running.
  runtimeError,
  // The user's code has more than one main method.
  twoMainMethods,
  // The user's code does not contain a main method.
  noMainMethod,
  // The user's code has a compiler error.
  compilerError,
  // The user tried to include a source file that did not end in .java
  javaExtensionMissing
}
