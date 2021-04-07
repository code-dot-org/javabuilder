package dev.javabuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.code.javabuilder.ProjectFile;
import org.code.javabuilder.ProjectFileManager;
import org.code.javabuilder.UserFacingException;

/** Intended for local testing only. Loads the MyClass.java file from the resources folder. */
public class LocalProjectFileManager implements ProjectFileManager {
  private ProjectFile file;

  @Override
  public void loadFiles() throws UserFacingException {
    this.file = new ProjectFile("MyClass.java");
    try {
      this.file.setCode(
          new String(
              Files.readAllBytes(
                  Paths.get(getClass().getClassLoader().getResource("MyClass.java").toURI()))));
    } catch (Exception e) {
      e.printStackTrace();
      throw new UserFacingException("We hit an error while loading your program. Try again.", e);
    }
  }

  @Override
  public ProjectFile getFile() {
    return this.file;
  }
}
