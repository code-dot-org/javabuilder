package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Logger;
import org.code.protocol.*;
import org.code.protocol.LoggerUtils.ClearStatus;
import org.code.protocol.LoggerUtils.SessionTime;

/**
 * Manages the asynchronous execution of code in a project. When started, it performs the necessary
 * pre-execution setup and starts code execution in a separate thread. When complete, or when asked
 * to interrupt execution, it ensures the necessary post-execution cleanup steps are performed.
 */
public class CodeExecutionManager {
  private final ProjectFileLoader fileLoader;
  private final InputHandler inputHandler;
  private final OutputAdapter outputAdapter;
  private final ExecutionType executionType;
  private final List<String> compileList;
  private final JavabuilderFileManager fileManager;
  private final LifecycleNotifier lifecycleNotifier;

  private File tempFolder;
  private InputRedirectionStream overrideInputStream;
  private OutputPrintStream overrideOutputStream;
  private InputStream systemInputStream;
  private PrintStream systemOutputStream;
  private boolean executionInProgress;

  public CodeExecutionManager(
      ProjectFileLoader fileLoader,
      InputHandler inputHandler,
      OutputAdapter outputAdapter,
      ExecutionType executionType,
      List<String> compileList,
      JavabuilderFileManager fileManager,
      LifecycleNotifier lifecycleNotifier) {
    this.fileLoader = fileLoader;
    this.inputHandler = inputHandler;
    this.outputAdapter = outputAdapter;
    this.executionType = executionType;
    this.compileList = compileList;
    this.fileManager = fileManager;
    this.lifecycleNotifier = lifecycleNotifier;
    this.executionInProgress = false;
  }

  public void execute() {
    try {
      this.onPreExecute();
      try {
        // Run code builder
        this.executionInProgress = true;
        final CodeBuilderRunnable runnable =
            new CodeBuilderRunnable(
                this.fileLoader,
                this.outputAdapter,
                this.tempFolder,
                this.executionType,
                this.compileList);
        runnable.run();
      } catch (Throwable e) {
        // Catch any throwable here to ensure that onPostExecute() is always called
        LoggerUtils.logException(e);
      }
      this.onPostExecute();
    } catch (InternalServerError e) {
      LoggerUtils.logError(e);
    }
  }

  /**
   * Request the code execution to cleanup early (e.g. in the case of an impending timeout). Code
   * execution may still run, but this allows for post execution steps to still occur before the
   * environment is shut down
   */
  public void requestEarlyExit() {
    if (!this.executionInProgress) {
      Logger.getLogger(MAIN_LOGGER).warning("Early exit requested before execution started");
      return;
    }
    try {
      this.onPostExecute();
      this.executionInProgress = false;
    } catch (InternalServerError e) {
      LoggerUtils.logError(e);
    }
  }

  /**
   * Pre-execution steps: 1) create temporary folder, 2) Replace System.in/out with custom in/out
   */
  private void onPreExecute() throws InternalServerError {
    // Create temp folder
    try {
      this.tempFolder = Files.createTempDirectory("tmpdir").toFile();
    } catch (IOException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }

    // Save System in/out and replace with custom in/out
    this.systemInputStream = System.in;
    this.systemOutputStream = System.out;
    this.overrideInputStream = new InputRedirectionStream(this.inputHandler);
    this.overrideOutputStream = new OutputPrintStream(this.outputAdapter);
    System.setOut(this.overrideOutputStream);
    System.setIn(this.overrideInputStream);
  }

  /**
   * Post-execution steps: 1) clean up global resources, 2) clear temporary folder, 3) close custom
   * in/out streams, 4) Replace System.in/out with original in/out, 5) Notify listeners
   */
  private void onPostExecute() throws InternalServerError {
    if (!this.executionInProgress) {
      Logger.getLogger(MAIN_LOGGER).warning("onPostExecute() called before execution started");
      return;
    }
    GlobalProtocol.getInstance().cleanUpResources();
    try {
      // Clear temp folder
      LoggerUtils.sendDiskSpaceUpdate(SessionTime.END_SESSION, ClearStatus.BEFORE_CLEAR);
      this.fileManager.cleanUpTempDirectory(this.tempFolder);
      LoggerUtils.sendDiskSpaceUpdate(SessionTime.END_SESSION, ClearStatus.AFTER_CLEAR);
      // Close custom input/output streams
      this.overrideInputStream.close();
      this.overrideOutputStream.close();
    } catch (IOException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
    // Replace System in/out with original System in/out
    System.setIn(this.systemInputStream);
    System.setOut(this.systemOutputStream);

    // Notify listeners
    this.lifecycleNotifier.onExecutionEnded();
  }
}
