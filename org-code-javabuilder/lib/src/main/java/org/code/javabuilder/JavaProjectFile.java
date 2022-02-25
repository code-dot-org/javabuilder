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
    // Check if file contents start with system import only because system import should always
    // be done by default by us, and therefore be the first thing in the file.
    // We still want to import System in the rare case where SYSTEM_IMPORT is in
    // a String somewhere in fileContents. In addition, a duplicate import will
    // not cause any error to the student, it just makes the file longer.
    if (!fileContents.startsWith(SYSTEM_IMPORT)) {
      return "import org.code.lang.System;\n" + fileContents;
    } else {
      return fileContents;
    }
  }
}
