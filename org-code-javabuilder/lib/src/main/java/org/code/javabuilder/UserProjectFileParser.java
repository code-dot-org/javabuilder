package org.code.javabuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
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
  public void parseFileJson(
      String json, List<JavaProjectFile> javaFileList, List<TextProjectFile> textFileList)
      throws UserFacingException, UserInitiatedException {
    try {
      UserSourceData sourceData = this.objectMapper.readValue(json, UserSourceData.class);
      Map<String, UserFileData> sources = sourceData.getSource();
      for (String fileName : sources.keySet()) {
        UserFileData fileData = sources.get(fileName);

        if (fileName.endsWith(".java")) {
          javaFileList.add(new JavaProjectFile(fileName, fileData.getText()));
        } else {
          // we treat any non-Java file as a plain text file
          textFileList.add(new TextProjectFile(fileName, fileData.getText()));
        }
      }
    } catch (IOException io) {
      throw new UserFacingException(
          "We hit an error trying to load your files. Please try again.\n", io);
    }
  }
}
