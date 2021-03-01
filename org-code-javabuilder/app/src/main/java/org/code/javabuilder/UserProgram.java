package org.code.javabuilder;

/** Logical representation of the user program received as a json object from the client. */
public class UserProgram {

  private String fileName;
  private String code;
  private String className;

  public UserProgram() {}

  public UserProgram(String fileName, String code) {
    this.fileName = fileName;
    this.code = code;
    this.className = null;
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
