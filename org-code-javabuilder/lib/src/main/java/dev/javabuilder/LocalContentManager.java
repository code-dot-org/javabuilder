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
import org.code.protocol.InternalExceptionKey;
import org.code.protocol.JavabuilderException;
import org.json.JSONException;

public class LocalContentManager implements ContentManager {
  private static final String SERVER_URL_FORMAT = "http://localhost:8080/%s/%s";

  private final ProjectData projectData;

  public LocalContentManager() throws InternalServerException {
    this.projectData = this.loadProjectData();
  }

  // Used for testing
  LocalContentManager(ProjectData projectData) {
    this.projectData = projectData;
  }

  public ProjectFileLoader getProjectFileLoader() {
    return this.projectData;
  }

  @Override
  public String getAssetUrl(String filename) {
    return this.projectData.getAssetUrl(filename);
  }

  @Override
  public String generateAssetUploadUrl(String filename) throws InternalServerException {
    final String uploadUrl = String.format(SERVER_URL_FORMAT, DIRECTORY, filename);
    // The URL to GET this asset once it is uploaded will be the same as the upload
    // URL since it points to the same location on the local file server. Add this
    // URL to our project data so it can be referenced later.
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
      throw new InternalServerException(InternalExceptionKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
    return String.format(SERVER_URL_FORMAT, DIRECTORY, filename);
  }

  @Override
  public void verifyAssetFilename(String filename) throws FileNotFoundException {
    if (!this.projectData.doesAssetUrlExist(filename)) {
      throw new FileNotFoundException(filename);
    }
  }

  private ProjectData loadProjectData() throws InternalServerException {
    final Path sourcesPath =
        Paths.get(
            System.getProperty("java.io.tmpdir"), DIRECTORY, ProjectData.PROJECT_DATA_FILE_NAME);
    try {
      return new ProjectData(Files.readString(sourcesPath));
    } catch (IOException | JSONException e) {
      // Error reading JSON file from local storage
      throw new InternalServerException(InternalExceptionKey.INTERNAL_EXCEPTION, e);
    }
  }
}
