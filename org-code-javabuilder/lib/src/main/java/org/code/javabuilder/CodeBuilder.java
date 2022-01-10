package org.code.javabuilder;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.code.protocol.*;
import org.code.protocol.LoggerUtils.ClearStatus;
import org.code.protocol.LoggerUtils.SessionTime;

/** The orchestrator for code compilation and execution. */
public class CodeBuilder implements AutoCloseable {
  private final OutputAdapter outputAdapter;
  private final InputHandler inputHandler;
  private final JavabuilderFileManager fileManager;
  private final File tempFolder;
  private final PrintStream sysout;
  private final InputStream sysin;
  private final UserProjectFiles userProjectFiles;

  public CodeBuilder(GlobalProtocol protocol, UserProjectFiles userProjectFiles)
      throws InternalServerError {
    this.sysout = System.out;
    this.sysin = System.in;
    this.outputAdapter = protocol.getOutputAdapter();
    this.inputHandler = protocol.getInputHandler();
    this.fileManager = protocol.getFileManager();
    this.userProjectFiles = userProjectFiles;
    try {
      this.tempFolder = Files.createTempDirectory("tmpdir").toFile();
    } catch (IOException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
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

  /** Runs all tests in the student's code */
  public void runUserTests() throws InternalFacingException, JavabuilderException {
    this.createJavaRunner().runTests();
  }

  /**
   * Resets System.in and System.out. Removes the temporary folder we generated to compile the
   * user's code.
   *
   * @throws InternalFacingException if the folder cannot be deleted.
   */
  @Override
  public void close() throws InternalFacingException {
    System.setOut(this.sysout);
    System.setIn(this.sysin);
    if (this.tempFolder != null) {
      try {
        // Clean up the temp folder and temp directory
        LoggerUtils.sendDiskSpaceUpdate(SessionTime.END_SESSION, ClearStatus.BEFORE_CLEAR);
        this.fileManager.cleanUpTempDirectory(this.tempFolder);
        LoggerUtils.sendDiskSpaceUpdate(SessionTime.END_SESSION, ClearStatus.AFTER_CLEAR);
      } catch (IOException e) {
        throw new InternalFacingException(e.toString(), e);
      }
    }
  }

  /**
   * Replaces System.in and System.out with our custom implementation and creates a runner for
   * executing code
   */
  private JavaRunner createJavaRunner() throws InternalServerError {
    System.setOut(new OutputPrintStream(this.outputAdapter));
    System.setIn(new InputRedirectionStream(this.inputHandler));
    try {
      return new JavaRunner(
          this.tempFolder.toURI().toURL(),
          this.userProjectFiles.getJavaFiles(),
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
