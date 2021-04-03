package org.code.javabuilder;

public class ProjectFile {
  private String fileName;
  private String code;
  private String className;

  // GOOD
  public ProjectFile(String fileName) throws Exception {
    this.fileName = fileName;
    if (fileName.indexOf(".java") > 0) {
      this.className = fileName.substring(0, fileName.indexOf(".java"));
    } else {
      throw new Exception("Invalid File Name. File name must end in '.java'.");
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
