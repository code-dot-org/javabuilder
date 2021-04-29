package org.code.javabuilder;

public interface ProjectFileLoader {
  UserProjectFiles loadFiles() throws UserFacingException, UserInitiatedException;
}
