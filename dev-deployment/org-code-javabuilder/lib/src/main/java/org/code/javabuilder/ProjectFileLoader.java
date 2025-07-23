package org.code.javabuilder;

public interface ProjectFileLoader {
  UserProjectFiles loadFiles() throws InternalServerException, UserInitiatedException;

  UserProjectFiles getValidation() throws InternalServerException, UserInitiatedException;
}
