package org.code.javabuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;

/** The orchestrator for code compilation and execution. */
public class CodeBuilder {
  private final OutputAdapter outputAdapter;
  private final InputAdapter inputAdapter;
  private final ProjectFileManager fileManager;
  private final File tempFolder;

  public CodeBuilder(
      InputAdapter inputAdapter, OutputAdapter outputAdapter, ProjectFileManager fileManager)
      throws UserFacingException {
    this.outputAdapter = outputAdapter;
    this.inputAdapter = inputAdapter;
    this.fileManager = fileManager;
    try {
      tempFolder = Files.createTempDirectory("tmpdir").toFile();
    } catch (IOException e) {
      throw new UserFacingException(
          "We hit an error on our side while loading your program. Try again.");
    }
  }

  /**
   * Loads and compiles the user's code
   *
   * @throws UserFacingException if the user's code contains a compiler error or if we are unable to
   *     compile due to internal errors.
   */
  public void compileUserCode() throws UserFacingException {
    this.fileManager.loadFiles();
    UserCodeCompiler codeCompiler = new UserCodeCompiler(fileManager, tempFolder, outputAdapter);
    codeCompiler.compileProgram();
  }

  /**
   * Replaces System.in and System.out with our custom implementation and executes the user's code.
   */
  public void runUserCode() throws UserFacingException, InternalFacingException {
    System.setOut(new OutputPrintStream(this.outputAdapter));
    System.setIn(new InputRedirectionStream(this.inputAdapter));
    JavaRunner runner;
    try {
      runner = new JavaRunner(tempFolder.toURI().toURL(), fileManager, outputAdapter);
    } catch (MalformedURLException e) {
      throw new UserFacingException(
          "We hit an error on our side while running your program. Try Again");
    }
    runner.runCode();
  }

  /**
   * Removes the temporary folder we generated to compile the user's code.
   *
   * @throws InternalFacingException if the folder cannot be deleted.
   */
  public void cleanUp() throws InternalFacingException {
    if (tempFolder != null) {
      try {
        Files.delete(tempFolder.toPath());
      } catch (IOException e) {
        throw new InternalFacingException(e.toString());
      }
    }
  }
}
