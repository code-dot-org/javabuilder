package dev.javabuilder;

import static dev.javabuilder.LocalWebserverConstants.DIRECTORY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.code.javabuilder.*;
import org.code.protocol.ContentManager;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.JavabuilderException;
import org.code.protocol.LoggerUtils;
import org.json.JSONException;

public class LocalContentManager implements ContentManager, ProjectFileLoader {
  private static final String SERVER_URL_FORMAT = "http://localhost:8080/%s/%s";

  private ProjectData projectData;

  public LocalContentManager() {
    this.projectData = null;
  }

  // Used for testing
  LocalContentManager(ProjectData projectData) {
    this.projectData = projectData;
  }

  @Override
  public UserProjectFiles loadFiles() throws InternalServerError, UserInitiatedException {
    this.loadProjectDataIfNeeded();
    return this.projectData.getSources();
  }

  @Override
  public String generateAssetUrl(String filename) {
    try {
      this.loadProjectDataIfNeeded();
    } catch (InternalServerError e) {
      // Log this error since this indicates an issue reading project data
      LoggerUtils.logException(e);
      return null;
    }
    return this.projectData.getAssetUrl(filename);
  }

  @Override
  public String generateAssetUploadUrl(String filename) throws InternalServerError {
    this.loadProjectDataIfNeeded();
    final String uploadUrl = String.format(SERVER_URL_FORMAT, DIRECTORY, filename);
    // Add new asset URL to our project data so it can be referenced later.
    this.projectData.addNewAssetUrl(filename, uploadUrl);
    return uploadUrl;
  }

  @Override
  public String writeToOutputFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException {
    final File file = Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY, filename).toFile();
    try {
      Files.write(file.toPath(), inputBytes);
    } catch (IOException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
    return String.format(SERVER_URL_FORMAT, DIRECTORY, filename);
  }

  @Override
  public void verifyAssetFilename(String filename) throws FileNotFoundException {
    if (!this.projectData.doesAssetUrlExist(filename)) {
      throw new FileNotFoundException(filename);
    }
  }

  // TODO: Project JSON data loading is deferred because it will not exist if we are
  // still using dashboard sources. Once we stop using dashboard sources, this step
  // can probably just happen immediately in the constructor.
  private void loadProjectDataIfNeeded() throws InternalServerError {
    if (this.projectData == null) {
      final Path sourcesPath =
          Paths.get(
              System.getProperty("java.io.tmpdir"), DIRECTORY, ProjectData.PROJECT_DATA_FILE_NAME);
      try {
        this.projectData = new ProjectData(Files.readString(sourcesPath));
      } catch (IOException | JSONException e) {
        // Error reading JSON file from local storage
        throw new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION, e);
      }
    }
  }
}
