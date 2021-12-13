package dev.javabuilder;

import static dev.javabuilder.LocalWebserverConstants.DIRECTORY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.code.javabuilder.InternalServerError;
import org.code.javabuilder.Util;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.JavabuilderException;
import org.code.protocol.JavabuilderFileManager;
import org.code.protocol.LoggerUtils;

public class LocalFileManager implements JavabuilderFileManager {

  private static final String SERVER_URL_FORMAT = "http://localhost:8080/%s/%s";

  @Override
  public String writeToFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException {
    File file = Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY, filename).toFile();
    try {
      File parentDirectory = Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY).toFile();
      if (!parentDirectory.exists()) {
        parentDirectory.mkdirs();
      }
      Files.write(file.toPath(), inputBytes);
    } catch (IOException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
    return String.format(SERVER_URL_FORMAT, DIRECTORY, filename);
  }

  @Override
  public String getUploadUrl(String filename) {
    File parentDirectory = Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY).toFile();
    if (!parentDirectory.exists()) {
      parentDirectory.mkdirs();
    }
    return String.format(SERVER_URL_FORMAT, DIRECTORY, filename);
  }

  @Override
  public URL getFileUrl(String filename) throws FileNotFoundException {
    try {
      // File path will be the same as the upload URL
      return new URL(this.getUploadUrl(filename));
    } catch (MalformedURLException e) {
      throw new FileNotFoundException(filename);
    }
  }

  @Override
  public void cleanUpTempDirectory(File tempFolder) throws IOException {
    if (tempFolder == null) {
      return;
    }
    // On localhost, we only need to clear the specific temp folder because
    // clearing the entire directory would clear the personal /tmp/ directory
    // in the user's local filesystem.
    Path toClear = tempFolder.toPath();
    LoggerUtils.sendDiskSpaceLogs();
    Util.recursivelyClearDirectory(toClear);
    LoggerUtils.sendClearedDirectoryLog(toClear);
    LoggerUtils.sendDiskSpaceLogs();
  }
}
