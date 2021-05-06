package org.code.javabuilder;

public enum UserInitiatedExceptionKey {
  illegalMethodAccess,
  runtimeError,
  // "Your code can only have one main method. We found at least two classes with main methods."
  twoMainMethods,
  // "Error: your program does not contain a main method"
  noMainMethod,
  // "We couldn't compile your program. Look for bugs in your program and try again."
  compilerError,
  // "Invalid File Name. File name must end in '.java'."
  javaExtensionMissing
}
