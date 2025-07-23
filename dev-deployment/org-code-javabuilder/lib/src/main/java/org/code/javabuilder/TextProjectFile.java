package org.code.javabuilder;

public class TextProjectFile implements ProjectFile {

  private String fileName;
  private String fileContents;

  public TextProjectFile(String fileName, String fileContents) {
    this.fileName = fileName;
    this.fileContents = fileContents;
  }

  @Override
  public String getFileName() {
    return this.fileName;
  }

  @Override
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public String getFileContents() {
    return this.fileContents;
  }

  @Override
  public void setFileContents(String fileContents) {
    this.fileContents = fileContents;
  }
}
