package org.code.javabuilder;

/** In-memory representation of a Java class that will be compiled. */
public class JavaProjectFile implements ProjectFile {
  private String fileName;
  private String fileContents;
  private String className;

  public JavaProjectFile(String fileName) throws UserInitiatedException {
    this.fileName = fileName;
    if (FileNameUtils.isJavaFile(fileName)) {
      this.className = fileName.substring(0, fileName.indexOf(FileNameUtils.JAVA_EXTENSION));
    } else {
      throw new UserInitiatedException(UserInitiatedExceptionKey.JAVA_EXTENSION_MISSING);
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
