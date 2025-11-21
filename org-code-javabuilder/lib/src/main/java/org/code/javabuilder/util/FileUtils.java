package org.code.javabuilder.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/** Convenience methods for handling file operations */
public final class FileUtils {
  private FileUtils() {
    throw new UnsupportedOperationException("Instantiation of utility class is not allowed.");
  }

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

  public static void recursivelyClearDirectory(Path directory) throws IOException {
    Files.walk(directory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }
}
