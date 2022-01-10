package org.code.javabuilder;

/** Convenience methods for handling file names */
public class FileNameUtils {
  public static final String JAVA_EXTENSION = ".java";

  /**
   * Whether the given file name is a valid Java file name. Must end with ".java" and have at least
   * one character before the extension.
   *
   * @param fileName file name to check
   * @return whether the file name is a valid Java file name
   */
  public static boolean isJavaFile(String fileName) {
    return fileName.endsWith(JAVA_EXTENSION) && fileName.indexOf(JAVA_EXTENSION) > 0;
  }
}
