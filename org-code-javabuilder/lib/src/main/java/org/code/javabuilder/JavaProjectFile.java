package org.code.javabuilder;

import org.code.javabuilder.util.FileUtils;

/** In-memory representation of a Java class that will be compiled. */
public class JavaProjectFile implements ProjectFile {
  private String fileName;
  private String fileContents;
  private String className;

  private static final String SYSTEM_IMPORT = "import org.code.lang.System;";

  public JavaProjectFile(String fileName) throws UserInitiatedException {
    this.fileName = fileName;
    if (FileUtils.isJavaFile(fileName)) {
      this.className = fileName.substring(0, fileName.indexOf(FileUtils.JAVA_EXTENSION));
    } else {
      throw new UserInitiatedException(UserInitiatedExceptionKey.JAVA_EXTENSION_MISSING);
    }
  }

  public JavaProjectFile(String fileName, String fileContents) throws UserInitiatedException {
    this(fileName);
    this.fileContents = this.importSystem(fileContents);
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
    this.fileContents = this.importSystem(fileContents);
  }

  public void setClassName(String className) {
    this.className = className;
  }

  private String importSystem(String fileContents) {
    // We always import system so users don't need to know about our custom
    // System class. A duplicate import will not cause any error to the user,
    // it just makes the file longer.
    return "import org.code.lang.System;\n" + fileContents;
  }
}
