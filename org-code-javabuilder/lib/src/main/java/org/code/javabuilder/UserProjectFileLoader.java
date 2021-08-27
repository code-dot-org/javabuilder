package org.code.javabuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.code.protocol.InternalErrorKey;

/** Manages the in-memory list of user project files. */
public class UserProjectFileLoader implements ProjectFileLoader {
  private final String baseUrl;
  private final UserProjectFileParser projectFileParser;
  private final boolean useNeighborhood;
  private final String levelId;
  private final String dashboardHostname;

  private final String MAIN_SOURCE_FILE_NAME = "main.json";

  public UserProjectFileLoader(
      String baseUrl, String levelId, String dashboardHostname, boolean useNeighborhood) {
    this.baseUrl = baseUrl;
    this.useNeighborhood = useNeighborhood;
    this.levelId = levelId;
    this.dashboardHostname = dashboardHostname;
    this.projectFileParser = new UserProjectFileParser();
  }

  /**
   * Loads all user project files.
   *
   * @throws InternalServerError If the load times out or fails.
   */
  @Override
  public UserProjectFiles loadFiles() throws InternalServerError, UserInitiatedException {
    HttpClient client = HttpClient.newBuilder().build();
    String jsonBody = getRequest(String.join("/", baseUrl, MAIN_SOURCE_FILE_NAME), client);
    UserProjectFiles projectFiles = this.projectFileParser.parseFileJson(jsonBody);
    this.loadNeighborhoodFile(projectFiles, client);
    // TODO: Support loading validation code
    return projectFiles;
  }

  public void loadNeighborhoodFile(UserProjectFiles userProjectFiles, HttpClient client)
      throws InternalServerError {
    if (this.useNeighborhood) {
      String requestUrl = this.dashboardHostname + "/levels/" + levelId + "/get_serialized_maze";
      String maze = this.getRequest(requestUrl, client);
      userProjectFiles.addTextFile(new TextProjectFile("grid.txt", maze));
    }
  }

  private String getRequest(String url, HttpClient client) throws InternalServerError {
    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(10)).build();
    HttpResponse<String> response;
    try {
      response = client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
    String body = response.body();
    if (response.statusCode() > 299) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION, new Exception(body));
    }
    return body;
  }
}
