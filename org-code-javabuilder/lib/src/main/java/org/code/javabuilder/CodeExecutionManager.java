package org.code.javabuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;
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

  /**
   * Convenience factory for creating the code builder thread. Useful for mocking during testing.
   */
  //  private static class CodeBuilderThreadFactory {
  //    public Thread createCodeBuilderThread(
  //        ProjectFileLoader fileLoader,
  //        OutputAdapter outputAdapter,
  //        File tempFolder,
  //        ExecutionType executionType,
  //        List<String> compileList,
  //        CompletionListener listener) {
  //      return new Thread(
  //          new CodeBuilderRunnable(
  //              fileLoader, outputAdapter, tempFolder, executionType, compileList, listener));
  //    }
  //  }

  //  public CodeExecutionManager(
  //      ProjectFileLoader fileLoader,
  //      InputHandler inputHandler,
  //      OutputAdapter outputAdapter,
  //      ExecutionType executionType,
  //      List<String> compileList,
  //      JavabuilderFileManager fileManager,
  //      LifecycleNotifier lifecycleNotifier) {
  //    this(
  //        fileLoader,
  //        inputHandler,
  //        outputAdapter,
  //        executionType,
  //        compileList,
  //        fileManager,
  //        lifecycleNotifier,
  //        new CodeBuilderThreadFactory());
  //  }

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
  }

  public void execute() {
    try {
      this.onPreExecute();
      try {
        // Run code builder
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

  //  /** Performs pre-execution setup and starts code execution in a separate thread. */
  ////  public void start() {
  ////    try {
  ////      this.onPreExecute();
  ////      this.executionThread =
  ////          this.threadFactory.createCodeBuilderThread(
  ////              this.fileLoader,
  ////              this.outputAdapter,
  ////              this.tempFolder,
  ////              this.executionType,
  ////              this.compileList,
  ////              this);
  ////      this.executionThread.start();
  ////    } catch (InternalServerError e) {
  ////      LoggerUtils.logError(e);
  ////    }
  ////  }
  //
  //  /** Interrupts the running code execution process. */
  ////  public void interrupt() {
  ////    try {
  ////      if (this.executionThread == null) {
  ////        // Will be caught and logged below
  ////        throw new InternalServerError(
  ////            InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION,
  ////            new IllegalStateException("Code execution interrupted before starting."));
  ////      }
  ////      this.executionThread.interrupt();
  ////      Logger.getLogger(MAIN_LOGGER).info("Calling post execute from interrupt.");
  ////      this.onPostExecute();
  ////    } catch (InternalServerError e) {
  ////      LoggerUtils.logError(e);
  ////    }
  ////  }
  //
  //  /** @return whether the code execution thread is alive */
  ////  public boolean isAlive() {
  ////    return this.executionThread != null && this.executionThread.isAlive();
  ////  }

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
