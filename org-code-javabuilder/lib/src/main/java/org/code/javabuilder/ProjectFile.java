package org.code.javabuilder;

public interface ProjectFile {
  String getFileName();

  void setFileName(String fileName);

  String getFileContents();

  void setFileContents(String fileContents);
}
