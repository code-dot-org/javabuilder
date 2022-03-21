package org.code.javabuilder;

import java.util.Map;
import org.code.protocol.InternalErrorKey;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents the JSON project data for a Javabuilder session. Expected structure:
 *
 * <pre>
 * {
 *   "sources": {
 *     "main.json": <main source file for a project>
 *     "grid.txt": <serialized maze if it exists>
 *   },
 *   "assetUrls": {"asset_name_1": <asset_url>, ...}
 *   "validation": <all validation code for a project, in json format>
 * }
 * </pre>
 */
public class ProjectData {
  public static final String PROJECT_DATA_FILE_NAME = "sources.json";
  // Expected JSON keys
  private static final String SOURCES_KEY = "sources";
  private static final String ASSET_URLS_KEY = "assetUrls";
  private static final String VALIDATION_KEY = "validation";

  private static final String MAIN_JSON_KEY = "main.json";
  private static final String MAZE_FILE_KEY = "grid.txt";

  private final JSONObject jsonData;
  private final UserProjectFileParser projectFileParser;

  public ProjectData(String json) throws JSONException {
    this(json, new UserProjectFileParser());
  }

  ProjectData(String json, UserProjectFileParser projectFileParser) throws JSONException {
    this.jsonData = new JSONObject(json);
    this.projectFileParser = projectFileParser;
  }

  public UserProjectFiles getSources() throws InternalServerError, UserInitiatedException {
    if (!this.jsonData.has(SOURCES_KEY)
        || !this.jsonData.getJSONObject(SOURCES_KEY).has(MAIN_JSON_KEY)) {
      throw new InternalServerError(
          InternalErrorKey.INTERNAL_EXCEPTION, new Exception("Code sources missing"));
    }
    final JSONObject sources = this.jsonData.getJSONObject(SOURCES_KEY);
    final UserProjectFiles projectFiles =
        this.projectFileParser.parseFileJson(sources.getString(MAIN_JSON_KEY));

    if (sources.has(MAZE_FILE_KEY)) {
      projectFiles.addTextFile(
          new TextProjectFile(MAZE_FILE_KEY, sources.getString(MAZE_FILE_KEY)));
    }
    return projectFiles;
  }

  public String getAssetUrl(String filename) {
    if (!this.jsonData.has(ASSET_URLS_KEY)
        || !this.jsonData.getJSONObject(ASSET_URLS_KEY).has(filename)) {
      return null;
    }

    return this.jsonData.getJSONObject(ASSET_URLS_KEY).getString(filename);
  }

  public boolean doesAssetUrlExist(String filename) {
    if (!this.jsonData.has(ASSET_URLS_KEY)) {
      return false;
    }

    return this.jsonData.getJSONObject(ASSET_URLS_KEY).has(filename);
  }

  public void addNewAssetUrl(String filename, String url) {
    if (!this.jsonData.has(ASSET_URLS_KEY)) {
      this.jsonData.put(ASSET_URLS_KEY, Map.of());
    }

    this.jsonData.getJSONObject(ASSET_URLS_KEY).put(filename, url);
  }

  public UserProjectFiles getValidation() throws InternalServerError, UserInitiatedException {
    if (!this.jsonData.has(VALIDATION_KEY)) {
      throw new InternalServerError(
          InternalErrorKey.INTERNAL_EXCEPTION, new Exception("Code sources missing"));
    }
    System.out.println();
    System.out.println(this.jsonData.getString(VALIDATION_KEY));
    final UserProjectFiles validationFiles =
        this.projectFileParser.parseFileJson(this.jsonData.getString(VALIDATION_KEY));
    return validationFiles;
  }
}
