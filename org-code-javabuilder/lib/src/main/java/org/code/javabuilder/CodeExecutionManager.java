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
public class CodeExecutionManager implements CompletionListener {
  private final ProjectFileLoader fileLoader;
  private final InputHandler inputHandler;
  private final OutputAdapter outputAdapter;
  private final ExecutionType executionType;
  private final List<String> compileList;
  private final JavabuilderFileManager fileManager;
  private final LifecycleNotifier lifecycleNotifier;
  private final CodeBuilderThreadFactory threadFactory;

  private Thread executionThread;
  private File tempFolder;
  private InputRedirectionStream overrideInputStream;
  private OutputPrintStream overrideOutputStream;
  private InputStream systemInputStream;
  private PrintStream systemOutputStream;

  /**
   * Convenience factory for creating the code builder thread. Useful for mocking during testing.
   */
  private static class CodeBuilderThreadFactory {
    public Thread createCodeBuilderThread(
        ProjectFileLoader fileLoader,
        OutputAdapter outputAdapter,
        File tempFolder,
        ExecutionType executionType,
        List<String> compileList,
        CompletionListener listener) {
      final CodeBuilderRunnable runnable =
          new CodeBuilderRunnable(
              fileLoader, outputAdapter, tempFolder, executionType, compileList, listener);

      return new Thread(
          () -> {
            final Thread codeThread = new Thread(runnable);
            codeThread.start();
            while (!Thread.currentThread().isInterrupted() && runnable.isRunning()) {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.getLogger(MAIN_LOGGER).info("Internal thread interrupted");
              }
            }
          });
      //
      //      return new Thread(
      //          new CodeBuilderRunnable(
      //              fileLoader, outputAdapter, tempFolder, executionType, compileList, listener));
    }
  }

  public CodeExecutionManager(
      ProjectFileLoader fileLoader,
      InputHandler inputHandler,
      OutputAdapter outputAdapter,
      ExecutionType executionType,
      List<String> compileList,
      JavabuilderFileManager fileManager,
      LifecycleNotifier lifecycleNotifier) {
    this(
        fileLoader,
        inputHandler,
        outputAdapter,
        executionType,
        compileList,
        fileManager,
        lifecycleNotifier,
        new CodeBuilderThreadFactory());
  }

  CodeExecutionManager(
      ProjectFileLoader fileLoader,
      InputHandler inputHandler,
      OutputAdapter outputAdapter,
      ExecutionType executionType,
      List<String> compileList,
      JavabuilderFileManager fileManager,
      LifecycleNotifier lifecycleNotifier,
      CodeBuilderThreadFactory threadFactory) {
    this.fileLoader = fileLoader;
    this.inputHandler = inputHandler;
    this.outputAdapter = outputAdapter;
    this.executionType = executionType;
    this.compileList = compileList;
    this.fileManager = fileManager;
    this.lifecycleNotifier = lifecycleNotifier;
    this.threadFactory = threadFactory;
  }

  /** Performs pre-execution setup and starts code execution in a separate thread. */
  public void start() {
    try {
      this.onPreExecute();
      this.executionThread =
          this.threadFactory.createCodeBuilderThread(
              this.fileLoader,
              this.outputAdapter,
              this.tempFolder,
              this.executionType,
              this.compileList,
              this);
      this.executionThread.start();
    } catch (InternalServerError e) {
      LoggerUtils.logError(e);
    }
  }

  /** Interrupts the running code execution process. */
  public void interrupt() {
    try {
      if (this.executionThread == null) {
        // Will be caught and logged below
        throw new InternalServerError(
            InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION,
            new IllegalStateException("Code execution interrupted before starting."));
      }
      this.executionThread.interrupt();
      Logger.getLogger(MAIN_LOGGER).info("Calling post execute from interrupt.");
      this.onPostExecute();
    } catch (InternalServerError e) {
      LoggerUtils.logError(e);
    }
  }

  /** @return whether the code execution thread is alive */
  public boolean isAlive() {
    return this.executionThread != null && this.executionThread.isAlive();
  }

  @Override
  public void onComplete() {
    try {
      Logger.getLogger(MAIN_LOGGER).info("Calling post execute from completion.");
      this.onPostExecute();
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
   * Post-execution steps: 1) clear temporary folder, 2) close custom in/out streams, 3) Replace
   * System.in/out with original in/out, 4) Notify listeners
   */
  private void onPostExecute() throws InternalServerError {
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
