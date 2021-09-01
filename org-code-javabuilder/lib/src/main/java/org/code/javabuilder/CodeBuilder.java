package org.code.javabuilder;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.code.protocol.*;

/** The orchestrator for code compilation and execution. */
public class CodeBuilder implements AutoCloseable {
  private final OutputAdapter outputAdapter;
  private final InputHandler inputHandler;
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
  public void buildUserCode() throws InternalServerError, UserInitiatedException {
    this.saveProjectAssets();
    UserCodeCompiler codeCompiler =
        new UserCodeCompiler(
            this.userProjectFiles.getJavaFiles(), this.tempFolder, this.outputAdapter);
    codeCompiler.compileProgram();
  }

  /**
   * Replaces System.in and System.out with our custom implementation and executes the user's code.
   */
  public void runUserCode()
      throws InternalServerError, InternalFacingException, UserInitiatedException {
    System.setOut(new OutputPrintStream(this.outputAdapter));
    System.setIn(new InputRedirectionStream(this.inputHandler));
    JavaRunner runner;
    try {
      runner =
          new JavaRunner(
              this.tempFolder.toURI().toURL(),
              this.userProjectFiles.getJavaFiles(),
              this.outputAdapter);
    } catch (MalformedURLException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
    runner.runCode();
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
        // Recursively delete the temp folder
        Util.recursivelyClearDirectory(this.tempFolder.toPath());
      } catch (IOException e) {
        throw new InternalFacingException(e.toString(), e);
      }
    }
  }

  /** Save any non-source code files to storage */
  private void saveProjectAssets() throws InternalServerError {
    // Save all text files to current folder.
    List<TextProjectFile> textProjectFiles = this.userProjectFiles.getTextFiles();
    for (TextProjectFile projectFile : textProjectFiles) {
      String filePath = projectFile.getFileName();
      try {
        Files.writeString(Path.of(filePath), projectFile.getFileContents());
      } catch (IOException e) {
        throw new InternalServerError(InternalErrorKey.INTERNAL_COMPILER_EXCEPTION, e);
      }
    }
  }
}
