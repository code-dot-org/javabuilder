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
  // The user's main method has an invalid signature.
  INVALID_MAIN_METHOD,
  // The user's code has a compiler error.
  COMPILER_ERROR,
  // The user tried to include a source file that did not end in .java
  JAVA_EXTENSION_MISSING,
  // The user is writing to a file in S3 too many times in a single session. The only file writing
  // that may happen is during a Theater project, when the theater image and theater audio files are
  // written to S3.
  TOO_MANY_WRITES,
  // The user is uploading a file to S3 too many times in a single session. The only file uploading
  // that may happen is during a Theater project using the Prompter feature.
  TOO_MANY_UPLOADS,
  // The user tried to run a file without a class definition
  CLASS_NOT_FOUND,
  // The user's code threw a FileNotFoundException
  FILE_NOT_FOUND,
  // The user's project has a Java file with an invalid file name
  INVALID_JAVA_FILE_NAME,
  // The user's project has a non-Java project file with a blank file name
  MISSING_PROJECT_FILE_NAME,
  // The supplied compile list was empty, or no matching Java files were found to compile (for the
  // COMPILE_ONLY option)
  NO_FILES_TO_COMPILE,
  // The user tried to use a class we don't allow.
  INVALID_CLASS
}
