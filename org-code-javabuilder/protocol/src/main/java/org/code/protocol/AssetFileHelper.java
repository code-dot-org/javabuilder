package org.code.protocol;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AssetFileHelper {
  private final String dashboardHostname;
  private final String channelId;
  private final String levelId;
  private final HttpClient httpClient;

  private List<String> starterAssetFilenames;
  private List<String> userAssetFilenames;

  public AssetFileHelper(String dashboardHostname, String channelId, String levelId) {
    this(dashboardHostname, channelId, levelId, HttpClient.newBuilder().build());
  }

  AssetFileHelper(
      String dashboardHostname, String channelId, String levelId, HttpClient httpClient) {
    this.dashboardHostname = dashboardHostname;
    this.channelId = channelId;
    this.levelId = levelId;
    this.httpClient = httpClient;
  }

  /**
   * Verifies that a file with the given filename exists in the user's project
   *
   * @param filename asset filename
   * @throws FileNotFoundException
   */
  public void verifyAssetFilename(String filename) throws FileNotFoundException {
    this.loadFilenameListsIfNeeded();

    if (!this.starterAssetFilenames.contains(filename)
        && !this.userAssetFilenames.contains(filename)) {
      throw new FileNotFoundException("Could not find file: " + filename);
    }
  }

  /**
   * Generates a URL for the file indicated by the given filename
   *
   * @param filename asset filename
   * @return asset URL
   */
  public String generateAssetUrl(String filename) {
    this.loadFilenameListsIfNeeded();

    return this.starterAssetFilenames.contains(filename)
        ? this.generateStarterAssetUrl(filename)
        : this.generateUserAssetUrl(filename);
  }

  private void loadFilenameListsIfNeeded() {
    if (this.starterAssetFilenames == null) {
      this.loadStarterAssetsList();
    }

    if (this.userAssetFilenames == null) {
      this.loadUserAssetsList();
    }
  }

  private void loadStarterAssetsList() {
    try {
      final JSONObject jsonResponse =
          new JSONObject(getRequest(this.generateStarterAssetsListUrl(), this.httpClient));
      this.starterAssetFilenames =
          !jsonResponse.has("starter_assets")
              ? Collections.emptyList()
              : jsonResponse
                  .getJSONArray("starter_assets")
                  .toList()
                  .stream()
                  // JSONArray.toList() converts contents to Maps and Lists
                  .filter(entry -> ((Map<String, Object>) entry).containsKey("filename"))
                  .map(entry -> (String) ((Map<String, Object>) entry).get("filename"))
                  .collect(Collectors.toList());
    } catch (JSONException e) {
      throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }

  private void loadUserAssetsList() {
    try {
      final JSONArray jsonResponse =
          new JSONArray(getRequest(this.generateUserAssetsListUrl(), this.httpClient));
      this.userAssetFilenames =
          jsonResponse
              .toList()
              .stream()
              // JSONArray.toList() converts contents to Maps and Lists
              .filter(entry -> ((Map<String, String>) entry).containsKey("filename"))
              .map(entry -> ((Map<String, String>) entry).get("filename"))
              .collect(Collectors.toList());
    } catch (JSONException e) {
      throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }

  private String generateUserAssetUrl(String filename) {
    // append timestamp to asset url to avoid cached 404s.
    return String.format(
        "%s/v3/assets/%s/%s?t=%d",
        this.dashboardHostname, this.channelId, filename, System.currentTimeMillis());
  }

  private String generateStarterAssetUrl(String filename) {
    // append timestamp to asset url to avoid cached 404s.
    return String.format(
        "%s/level_starter_assets/%s/%s?t=%d",
        this.dashboardHostname, this.levelId, filename, System.currentTimeMillis());
  }

  private String generateStarterAssetsListUrl() {
    return String.format("%s/level_starter_assets/%s", this.dashboardHostname, this.levelId);
  }

  private String generateUserAssetsListUrl() {
    return String.format("%s/v3/assets/%s", this.dashboardHostname, this.channelId);
  }

  // TODO: Create generic HTTP request handler and share with UserProjectFileLoader
  private String getRequest(String url, HttpClient client) throws InternalServerRuntimeError {
    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(10)).build();
    HttpResponse<String> response;
    try {
      response = client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
    String body = response.body();
    if (response.statusCode() > 299) {
      throw new InternalServerRuntimeError(
          InternalErrorKey.INTERNAL_EXCEPTION, new Exception(body));
    }
    return body;
  }
}
