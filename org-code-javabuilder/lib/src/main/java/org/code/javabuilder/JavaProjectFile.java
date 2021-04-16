package org.code.javabuilder;

/** In-memory representation of a Java class that will be compiled. */
public class JavaProjectFile implements ProjectFile {
  private String fileName;
  private String fileContents;
  private String className;

  public JavaProjectFile(String fileName) throws UserInitiatedException {
    this.fileName = fileName;
    if (fileName.indexOf(".java") > 0) {
      this.className = fileName.substring(0, fileName.indexOf(".java"));
    } else {
      throw new UserInitiatedException("Invalid File Name. File name must end in '.java'.");
    }
  }

  public JavaProjectFile(String fileName, String fileContents) throws UserInitiatedException {
    this(fileName);
    this.fileContents = fileContents;
  }

  @Override
  public String getFileName() {
    return fileName;
  }

  @Override
  public String getFileContents() {
    return this.fileContents;
  }

  public String getClassName() {
    return this.className;
  }

  @Override
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public void setFileContents(String fileContents) {
    this.fileContents = fileContents;
  }

  public void setClassName(String className) {
    this.className = className;
  }
}
