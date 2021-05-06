package org.code.javabuilder;

public enum UserFacingExceptionKey {
  // "We hit an error on our side while running your program. Try Again"
  internalRuntimeException,
  // "We hit an error on our side while compiling your program. Try again."
  internalCompilerException,
  // "We hit an error on our side while loading your program. Try again."
  internalException
}
