package org.code.protocol;

import java.io.File;
import java.io.IOException;

/** Handles cleaning up the temp directory used during program execution */
public interface TempDirectoryManager {
  /**
   * Cleans up the temp directory used during program execution.
   *
   * @param tempFolder the temp folder used during program execution. May be null if not present
   * @throws IOException if there is an issue deleting files
   */
  void cleanUpTempDirectory(File tempFolder) throws IOException;
}
