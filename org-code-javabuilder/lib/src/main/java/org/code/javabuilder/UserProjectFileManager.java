package org.code.javabuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;

/** Manages the in-memory list of user project files. */
public class UserProjectFileManager implements ProjectFileManager {
  private final String baseUrl;
  private final ArrayList<ProjectFile> fileList;
  private final ObjectMapper objectMapper;

  private final String MAIN_SOURCE_FILE_NAME = "main.json";

  public UserProjectFileManager(String baseUrl) {
    this.baseUrl = baseUrl;
    this.fileList = new ArrayList<>();
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Loads all user project files.
   *
   * @throws UserFacingException If the load times out or fails.
   */
  public void loadFiles() throws UserFacingException, UserInitiatedException {
    HttpClient client = HttpClient.newBuilder().build();
    // TODO: Support loading validation code
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(String.join("/", baseUrl, MAIN_SOURCE_FILE_NAME)))
            .timeout(Duration.ofSeconds(10))
            .build();
    HttpResponse<String> response;
    try {
      response = client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      throw new UserFacingException(
          "We hit an error on our side while loading your files. Try again. \n", e);
    }
    String body = response.body();
    if (response.statusCode() > 299) {
      throw new UserFacingException(
          "We hit an error on our side while loading your files. Try again. \n",
          new Exception(body));
    }
    this.parseFiles(body);
  }

  /**
   * Returns the user project file. Currently, we only allow a single user project file. TODO:
   * Enable multi-file programs
   *
   * @return the user project file.
   */
  @Override
  public ProjectFile getFile() {
    // TODO: Enable multi-file. For now, load the first file.
    return fileList.get(0);
  }

  /**
   * Parses json string containing file data and stores it in this.fileList.
   *
   * @param body JSON String in UserSourceData format
   * @throws UserFacingException
   * @throws UserInitiatedException
   */
  private void parseFiles(String body) throws UserFacingException, UserInitiatedException {
    try {
      UserSourceData sourceData = this.objectMapper.readValue(body, UserSourceData.class);
      Map<String, UserFileData> sources = sourceData.getSource();
      for (String fileName : sources.keySet()) {
        UserFileData fileData = sources.get(fileName);
        fileList.add(new ProjectFile(fileName, fileData.getText()));
      }
    } catch (IOException io) {
      throw new UserFacingException(
          "We hit an error trying to load your files. Please try again.\n", io);
    }
  }
}
