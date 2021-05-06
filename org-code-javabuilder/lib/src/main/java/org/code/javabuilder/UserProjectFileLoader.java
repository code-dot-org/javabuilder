package org.code.javabuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Manages the in-memory list of user project files. */
public class UserProjectFileLoader implements ProjectFileLoader {
  private final String baseUrl;
  private final UserProjectFileParser projectFileParser;

  private final String MAIN_SOURCE_FILE_NAME = "main.json";

  public UserProjectFileLoader(String baseUrl) {
    this.baseUrl = baseUrl;
    this.projectFileParser = new UserProjectFileParser();
  }

  /**
   * Loads all user project files.
   *
   * @throws UserFacingException If the load times out or fails.
   */
  @Override
  public UserProjectFiles loadFiles() throws UserFacingException, UserInitiatedException {
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
      throw new UserFacingException(UserFacingExceptionKey.internalException, e);
    }
    String body = response.body();
    if (response.statusCode() > 299) {
      throw new UserFacingException(UserFacingExceptionKey.internalException, new Exception(body));
    }
    return this.projectFileParser.parseFileJson(body);
  }
}
