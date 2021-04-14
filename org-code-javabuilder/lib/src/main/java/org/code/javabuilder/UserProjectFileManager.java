package org.code.javabuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;

/** Manages the in-memory list of user project files. */
public class UserProjectFileManager implements ProjectFileManager {
  private final String baseUrl;
  private final String[] fileNames;
  private final ArrayList<ProjectFile> fileList;

  public UserProjectFileManager(String baseUrl, String[] fileNames) {
    this.baseUrl = baseUrl;
    this.fileNames = fileNames;
    this.fileList = new ArrayList<>();
  }

  /**
   * Loads all user project files.
   *
   * @throws UserFacingException If the load times out or fails.
   */
  public void loadFiles() throws UserFacingException, UserInitiatedException {
    System.out.println("Loading files");
    HttpClient client = HttpClient.newBuilder().build();
    // TODO: Enable multi-file. For now, we will always have exactly one file.
    String fileName = fileNames[0];
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(String.join("/", baseUrl, fileName)))
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
    ProjectFile projectFile = new ProjectFile(fileName);
    projectFile.setCode(body);
    fileList.add(projectFile);
  }

  /**
   * Returns the user project file. Currently, we only allow a single user project file. TODO:
   * Enable multi-file programs
   *
   * @return the user project file.
   */
  @Override
  public ProjectFile getFile() {
    // TODO: Enable multi-file. For now, we will always have exactly one file.
    return fileList.get(0);
  }
}
