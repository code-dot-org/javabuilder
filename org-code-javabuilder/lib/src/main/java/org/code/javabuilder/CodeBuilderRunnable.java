package org.code.javabuilder;

import java.io.File;
import java.util.List;
import org.code.protocol.*;

/**
 * Encapsulates the compilation and execution of code in a project. TODO: This is no longer run in a
 * thread and is no longer a runnable, so it should be renamed for clarity.
 */
public class CodeBuilderRunnable {
  private final ProjectFileLoader fileLoader;
  private final File tempFolder;
  private final ExecutionType executionType;
  private final List<String> compileList;

  public CodeBuilderRunnable(
      ProjectFileLoader fileLoader,
      File tempFolder,
      ExecutionType executionType,
      List<String> compileList) {
    this.fileLoader = fileLoader;
    this.tempFolder = tempFolder;
    this.executionType = executionType;
    this.compileList = compileList;
  }

  public void run() throws JavabuilderException, InternalFacingException {
    this.executeCodeBuilder();
  }

  private void executeCodeBuilder() throws JavabuilderException, InternalFacingException {
    UserProjectFiles userProjectFiles = fileLoader.loadFiles();
    UserProjectFiles validationFiles = fileLoader.getValidation();
    CodeBuilder codeBuilder =
        new CodeBuilder(
            GlobalProtocol.getInstance(), userProjectFiles, validationFiles, this.tempFolder);
    switch (this.executionType) {
      case COMPILE_ONLY:
        codeBuilder.buildUserCode(this.compileList);
        break;
      case RUN:
        codeBuilder.buildAllUserCode();
        codeBuilder.runUserCode();
        break;
      case TEST:
        codeBuilder.buildUserAndValidationFiles();
        codeBuilder.runTests();
        break;
    }
  }
}
