package dev.javabuilder;

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
  }
}
