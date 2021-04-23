package org.code.javabuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

public class UserProjectFileParser {
  private final ObjectMapper objectMapper;

  public UserProjectFileParser() {
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Parses json string containing file data and returns a list of project files.
   *
   * @param json JSON String in UserSourceData format
   * @return a list of ProjectFile objects
   * @throws UserFacingException
   * @throws UserInitiatedException
   */
  public UserProjectFiles parseFileJson(String json)
      throws UserFacingException, UserInitiatedException {
    try {
      UserProjectFiles userProjectFiles = new UserProjectFiles();
      UserSourceData sourceData = this.objectMapper.readValue(json, UserSourceData.class);
      Map<String, UserFileData> sources = sourceData.getSource();
      for (String fileName : sources.keySet()) {
        UserFileData fileData = sources.get(fileName);

        if (fileName.endsWith(".java")) {
          userProjectFiles.addJavaFile(new JavaProjectFile(fileName, fileData.getText()));
        } else {
          // we treat any non-Java file as a plain text file
          userProjectFiles.addTextFile(new TextProjectFile(fileName, fileData.getText()));
        }
      }
      return userProjectFiles;
    } catch (IOException io) {
      throw new UserFacingException(
          "We hit an error trying to load your files. Please try again.\n", io);
    }
  }
}
