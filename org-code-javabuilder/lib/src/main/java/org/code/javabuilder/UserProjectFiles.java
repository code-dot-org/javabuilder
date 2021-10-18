package org.code.javabuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** In memory representation of all files in a user's project. */
public class UserProjectFiles {
  private final List<JavaProjectFile> javaFiles;
  private final List<TextProjectFile> textFiles;

  public UserProjectFiles() {
    this.javaFiles = new ArrayList<>();
    this.textFiles = new ArrayList<>();
  }

  public void addJavaFile(JavaProjectFile javaFile) {
    this.javaFiles.add(javaFile);
  }

  public void addTextFile(TextProjectFile textFile) {
    this.textFiles.add(textFile);
  }

  public List<JavaProjectFile> getJavaFiles() {
    return this.javaFiles;
  }

  public List<TextProjectFile> getTextFiles() {
    return this.textFiles;
  }

  public List<JavaProjectFile> getMatchingJavaFiles(List<String> filenames) {
    return this.getMatchingProjectFiles(filenames, this.javaFiles);
  }

  public List<TextProjectFile> getMatchingTextFiles(List<String> filenames) {
    return this.getMatchingProjectFiles(filenames, this.textFiles);
  }

  private <T extends ProjectFile> List<T> getMatchingProjectFiles(
      List<String> filenames, List<T> projectFiles) {
    return projectFiles
        .stream()
        .filter(projectFile -> filenames.contains(projectFile.getFileName()))
        .collect(Collectors.toList());
  }
}
