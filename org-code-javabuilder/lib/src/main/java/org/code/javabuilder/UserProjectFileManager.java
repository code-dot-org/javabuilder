package org.code.javabuilder;

public class UserProjectFileManager implements ProjectFileManager {

  public UserProjectFileManager(String baseUrl, String[] fileNames) {}

  public void loadFiles() throws UserFacingException {
    throw new UserFacingException("We couldn't load your files");
  }

  @Override
  public ProjectFile getFile() {
    return null;
  }
}
