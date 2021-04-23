package org.code.javabuilder;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Handles basic file operations for a user project: in-memory representation of user files saving
 * assets to local storage for future use.
 */
public class ProjectFileManager {
  private UserProjectFiles userProjectFiles;

  public ProjectFileManager(UserProjectFiles userProjectFiles) {
    this.userProjectFiles = userProjectFiles;
  }

  /** @return the user's project assets */
  public UserProjectFiles getUserProjectFiles() {
    return this.userProjectFiles;
  }

  /** Save any non-source code files to storage */
  public void saveProjectAssets() throws UserFacingException {
    // Save all text files to current folder.
    List<TextProjectFile> textProjectFiles = this.getUserProjectFiles().getTextFiles();
    for (TextProjectFile projectFile : textProjectFiles) {
      String filePath = projectFile.getFileName();
      try (PrintWriter out = new PrintWriter(filePath)) {
        out.println(projectFile.getFileContents());
      } catch (FileNotFoundException e) {
        throw new UserFacingException(
            "We hit an error on our side while compiling your program. Try again.", e);
      }
    }
  }
}
