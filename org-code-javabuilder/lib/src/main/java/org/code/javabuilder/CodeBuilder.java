package org.code.javabuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.JavabuilderException;
import org.code.protocol.OutputAdapter;

/** The orchestrator for code compilation and execution. */
public class CodeBuilder {
  private final OutputAdapter outputAdapter;
  private final File tempFolder;
  private final UserProjectFiles userProjectFiles;
  private final UserProjectFiles validationFiles;

  public CodeBuilder(
      GlobalProtocol protocol,
      UserProjectFiles userProjectFiles,
      UserProjectFiles validationFiles,
      File tempFolder)
      throws InternalServerError {
    this.outputAdapter = protocol.getOutputAdapter();
    this.userProjectFiles = userProjectFiles;
    this.validationFiles = validationFiles;
    this.tempFolder = tempFolder;
  }

  /**
   * Saves non-source code assets to storage and compiles the user's code.
   *
   * @throws InternalServerError if the user's code contains a compiler error or if we are unable to
   *     compile due to internal errors.
   */
  public void buildAllUserCode() throws InternalServerError, UserInitiatedException {
    this.compileCode(this.userProjectFiles.getJavaFiles());
  }

  /**
   * Saves non-source code assets to storage and compiles a subset of the user's code.
   *
   * @param compileList a list of file names to compile
   * @throws InternalServerError if there is an internal error compiling or saving
   * @throws UserInitiatedException if no matching file names are found, or there is an issue
   *     compiling
   */
  public void buildUserCode(List<String> compileList)
      throws InternalServerError, UserInitiatedException {
    if (compileList == null) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.NO_FILES_TO_COMPILE);
    }
    final List<JavaProjectFile> javaProjectFiles =
        this.userProjectFiles.getMatchingJavaFiles(compileList);

    this.compileCode(javaProjectFiles);
  }

  public void buildValidation() throws InternalServerError, UserInitiatedException {
    this.compileCode(this.validationFiles.getJavaFiles());
  }

  private void compileCode(List<JavaProjectFile> javaProjectFiles)
      throws InternalServerError, UserInitiatedException {
    if (javaProjectFiles.isEmpty()) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.NO_FILES_TO_COMPILE);
    }

    this.saveProjectAssets();
    UserCodeCompiler codeCompiler =
        new UserCodeCompiler(javaProjectFiles, this.tempFolder, this.outputAdapter);
    codeCompiler.compileProgram();
  }

  /** Runs the main method of the student's code */
  public void runUserCode() throws InternalFacingException, JavabuilderException {
    this.createJavaRunner().runMain();
  }

  /** Runs all tests in the student's code and any validation provided for the code */
  public void runTests() throws InternalFacingException, JavabuilderException {
    this.createJavaRunner().runTests();
  }

  /** Creates a runner for executing code */
  private JavaRunner createJavaRunner() throws InternalServerError {
    try {
      return new JavaRunner(
          this.tempFolder.toURI().toURL(),
          this.userProjectFiles.getJavaFiles(),
          this.validationFiles.getJavaFiles(),
          this.outputAdapter);
    } catch (MalformedURLException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
  }

  /** Save any non-source code files to storage */
  private void saveProjectAssets() throws InternalServerError, UserInitiatedException {
    // Save all text files to current folder.
    List<TextProjectFile> textProjectFiles = this.userProjectFiles.getTextFiles();
    for (TextProjectFile projectFile : textProjectFiles) {
      String filePath = projectFile.getFileName();
      try {
        Files.writeString(Path.of(filePath), projectFile.getFileContents());
      } catch (IOException e) {
        if (filePath.isBlank()) {
          // If the file name is empty, indicate to the user that the file name is invalid
          throw new UserInitiatedException(UserInitiatedExceptionKey.MISSING_PROJECT_FILE_NAME, e);
        }
        throw new InternalServerError(InternalErrorKey.INTERNAL_COMPILER_EXCEPTION, e);
      }
    }
  }
}
