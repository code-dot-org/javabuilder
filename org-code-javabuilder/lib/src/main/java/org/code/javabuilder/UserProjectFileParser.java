package org.code.javabuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserProjectFileParser {
  private final ObjectMapper objectMapper;

  public UserProjectFileParser() {
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Parses json string containing file data and stores it in this.fileList.
   *
   * @param json JSON String in UserSourceData format
   * @throws UserFacingException
   * @throws UserInitiatedException
   */
  public List<ProjectFile> parseFileJson(String json)
      throws UserFacingException, UserInitiatedException {
    List<ProjectFile> fileList = new ArrayList<ProjectFile>();
    try {
      UserSourceData sourceData = this.objectMapper.readValue(json, UserSourceData.class);
      Map<String, UserFileData> sources = sourceData.getSource();
      for (String fileName : sources.keySet()) {
        UserFileData fileData = sources.get(fileName);
        fileList.add(new ProjectFile(fileName, fileData.getText()));
      }
    } catch (IOException io) {
      throw new UserFacingException(
          "We hit an error trying to load your files. Please try again.\n", io);
    }
    return fileList;
  }
}
