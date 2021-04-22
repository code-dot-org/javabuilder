package dev.javabuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.code.javabuilder.*;

/** Intended for local testing only. Loads the main.json file from the resources folder. */
public class LocalProjectFileManager implements ProjectFileManager {
  private List<JavaProjectFile> javaFiles;
  private List<TextProjectFile> textFiles;

  @Override
  public void loadFiles() throws UserFacingException, UserInitiatedException {
    this.javaFiles = new ArrayList<>();
    this.textFiles = new ArrayList<>();
    try {
      String mainJson =
          new String(
              Files.readAllBytes(
                  Paths.get(getClass().getClassLoader().getResource("main.json").toURI())));
      new UserProjectFileParser().parseFileJson(mainJson, javaFiles, textFiles);
    } catch (IOException | URISyntaxException e) {
      throw new UserFacingException("We could not parse your files", e);
    }
  }

  @Override
  public List<JavaProjectFile> getJavaFiles() {
    return this.javaFiles;
  }

  @Override
  public List<TextProjectFile> getTextFiles() {
    return this.textFiles;
  }
}
