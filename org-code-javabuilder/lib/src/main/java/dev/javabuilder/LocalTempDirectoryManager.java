package dev.javabuilder;

import dev.javabuilder.util.LocalStorageUtils;
import java.io.File;
import java.io.IOException;
import org.code.javabuilder.util.FileUtils;
import org.code.protocol.TempDirectoryManager;

public class LocalTempDirectoryManager implements TempDirectoryManager {

  @Override
  public void cleanUpTempDirectory(File tempFolder) throws IOException {
    if (tempFolder == null) {
      return;
    }
    // On localhost, we only need to clear the specific temp folder because
    // clearing the entire directory would clear the personal /tmp/ directory
    // in the user's local filesystem.
    FileUtils.recursivelyClearDirectory(tempFolder.toPath());
    // Also, clear the local storage directory where project sources and output is stored
    FileUtils.recursivelyClearDirectory(LocalStorageUtils.getLocalStoragePath());
  }
}
