package dev.javabuilder.util;

import static dev.javabuilder.LocalWebserverConstants.DIRECTORY;

import java.io.File;
import java.nio.file.Paths;

public final class TempDirectoryUtils {
  private TempDirectoryUtils() {
    throw new UnsupportedOperationException("Instantiation of utility class is not allowed.");
  }

  public static void createTempDirectoryIfNeeded() {
    final File parentDirectory =
        Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY).toFile();
    if (!parentDirectory.exists()) {
      parentDirectory.mkdirs();
    }
  }
}
