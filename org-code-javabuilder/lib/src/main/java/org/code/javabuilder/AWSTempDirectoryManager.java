package org.code.javabuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.code.javabuilder.util.FileUtils;
import org.code.protocol.*;

public class AWSTempDirectoryManager implements TempDirectoryManager {

  @Override
  public void cleanUpTempDirectory(File tempFolder) throws IOException {
    // Delete any leftover contents of the tmp folder from previous lambda invocations
    // We can ignore the tempFolder since we are clearing the entire directory
    FileUtils.recursivelyClearDirectory(Paths.get(System.getProperty("java.io.tmpdir")));
  }
}
