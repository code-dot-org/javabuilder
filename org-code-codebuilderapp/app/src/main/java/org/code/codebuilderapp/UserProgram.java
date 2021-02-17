package org.code.codebuilderapp;

/**
 * Logical representation of the user program received as a json object from the
 * client.
 */
public class UserProgram {

  private String fileName;
  private String code;

  public UserProgram() {}

  public UserProgram(String fileName, String code) {
    this.fileName = fileName;
    this.code = code;
  }

  public String getFileName() {
    return fileName;
  }

  public String getCode() {
    return code;
  }

  public void getFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
