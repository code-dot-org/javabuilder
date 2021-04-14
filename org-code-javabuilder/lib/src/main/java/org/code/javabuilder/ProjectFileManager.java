package org.code.javabuilder;

import java.util.List;

/**
 * Handles loading user files (i.e. from local storage or via HTTP) and manages the in-memory
 * representation of them.
 */
public interface ProjectFileManager {
  /**
   * Loads the user's Java files from HTTP or local storage
   *
   * @throws UserFacingException If an error occurs while loading
   */
  void loadFiles() throws UserFacingException, UserInitiatedException;

  /** @return The user's Java files. */
  List<ProjectFile> getFiles();
}
