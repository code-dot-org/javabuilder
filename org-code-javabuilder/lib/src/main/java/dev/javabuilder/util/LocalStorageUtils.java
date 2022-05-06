package dev.javabuilder.util;

import static dev.javabuilder.LocalWebserverConstants.DIRECTORY;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages access to the local storage location used to store project data (sources, output) when
 * running Javabuilder on localhost. This is analogous to S3 in the production Javabuilder stack.
 */
public final class LocalStorageUtils {
  private LocalStorageUtils() {
    throw new UnsupportedOperationException("Instantiation of utility class is not allowed.");
  }

  public static void createLocalStorageIfNeeded() {
    final File parentDirectory =
        Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY).toFile();
    if (!parentDirectory.exists()) {
      parentDirectory.mkdirs();
    }
  }

  public static Path getLocalFilePath(String fileName) {
    return Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY, fileName);
  }

  public static Path getLocalStoragePath() {
    return Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY);
  }
}
