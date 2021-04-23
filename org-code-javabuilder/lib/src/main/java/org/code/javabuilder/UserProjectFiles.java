package org.code.javabuilder;

import java.util.ArrayList;
import java.util.List;

public class UserProjectFiles {
  private List<JavaProjectFile> javaFiles;
  private List<TextProjectFile> textFiles;

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
}
