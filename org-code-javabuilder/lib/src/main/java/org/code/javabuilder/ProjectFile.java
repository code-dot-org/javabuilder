package org.code.javabuilder;

/** In-memory representation of a Java class that will be compiled. */
public class ProjectFile {
  private String fileName;
  private String code;
  private String className;

  public ProjectFile(String fileName) throws UserInitiatedException {
    this.fileName = fileName;
    if (fileName.indexOf(".java") > 0) {
      this.className = fileName.substring(0, fileName.indexOf(".java"));
    } else {
      throw new UserInitiatedException("Invalid File Name. File name must end in '.java'.");
    }
  }

  public String getFileName() {
    return fileName;
  }

  public String getCode() {
    return code;
  }

  public String getClassName() {
    return this.className;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setClassName(String className) {
    this.className = className;
  }
}
