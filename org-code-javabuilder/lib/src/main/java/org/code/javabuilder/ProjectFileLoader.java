package org.code.javabuilder;

public interface ProjectFileLoader {
  UserProjectFiles loadFiles() throws InternalServerError, UserInitiatedException;

  UserProjectFiles getValidation() throws InternalServerError, UserInitiatedException;
}
