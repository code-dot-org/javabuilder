package org.code.javabuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Manages the in-memory list of user project files. */
public class UserProjectFileManager implements ProjectFileManager {
  private final String baseUrl;
  private final UserProjectFileParser projectFileParser;
  private List<JavaProjectFile> javaFileList;
  private List<TextProjectFile> textFileList;

  private final String MAIN_SOURCE_FILE_NAME = "main.json";

  public UserProjectFileManager(String baseUrl) {
    this.baseUrl = baseUrl;
    this.javaFileList = new ArrayList<>();
    this.textFileList = new ArrayList<>();
    this.projectFileParser = new UserProjectFileParser();
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
    this.projectFileParser.parseFileJson(body, this.javaFileList, this.textFileList);
  }

  /**
   * Return the user's Java project files
   *
   * @return A list of Java project files, which may be empty if files have not been loaded.
   */
  @Override
  public List<JavaProjectFile> getJavaFiles() {
    return this.javaFileList;
  }

  /**
   * Return the user's text project files
   *
   * @return A list of text project files, which may be empty if files have not been loaded or if
   *     there are no text files.
   */
  @Override
  public List<TextProjectFile> getTextFiles() {
    return this.textFileList;
  }
}
