package org.code.javabuilder;

import static org.code.javabuilder.LambdaErrorCodes.TEMP_DIRECTORY_CLEANUP_ERROR_CODE;
import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Logger;
import org.code.protocol.*;

/**
 * Manages the execution of code in a project. When started, it performs the necessary pre-execution
 * setup and starts code execution. When complete, or when asked to exit early, it ensures the
 * necessary post-execution cleanup steps are performed.
 */
public class CodeExecutionManager {
  private final ProjectFileLoader fileLoader;
  private final InputAdapter inputAdapter;
  private final OutputAdapter outputAdapter;
  private final ExecutionType executionType;
  private final List<String> compileList;
  private final TempDirectoryManager tempDirectoryManager;
  private final LifecycleNotifier lifecycleNotifier;
  private final ContentManager contentManager;
  private final SystemExitHelper systemExitHelper;
  private final CodeBuilderRunnableFactory codeBuilderRunnableFactory;

  private File tempFolder;
  private InputRedirectionStream overrideInputStream;
  private OutputPrintStream overrideOutputStream;
  private InputStream systemInputStream;
  private PrintStream systemOutputStream;
  private boolean executionInProgress;

  static class CodeBuilderRunnableFactory {
    public CodeBuilderRunnable createCodeBuilderRunnable(
        ProjectFileLoader fileLoader,
        OutputAdapter outputAdapter,
        File tempFolder,
        ExecutionType executionType,
        List<String> compileList) {
      return new CodeBuilderRunnable(
          fileLoader, outputAdapter, tempFolder, executionType, compileList);
    }
  }

  public CodeExecutionManager(
      ProjectFileLoader fileLoader,
      InputAdapter inputAdapter,
      OutputAdapter outputAdapter,
      ExecutionType executionType,
      List<String> compileList,
      TempDirectoryManager tempDirectoryManager,
      ContentManager contentManager,
      LifecycleNotifier lifecycleNotifier,
      SystemExitHelper systemExitHelper) {
    this(
        fileLoader,
        inputAdapter,
        outputAdapter,
        executionType,
        compileList,
        tempDirectoryManager,
        lifecycleNotifier,
        contentManager,
        systemExitHelper,
        new CodeBuilderRunnableFactory());
  }

  CodeExecutionManager(
      ProjectFileLoader fileLoader,
      InputAdapter inputAdapter,
      OutputAdapter outputAdapter,
      ExecutionType executionType,
      List<String> compileList,
      TempDirectoryManager tempDirectoryManager,
      LifecycleNotifier lifecycleNotifier,
      ContentManager contentManager,
      SystemExitHelper systemExitHelper,
      CodeBuilderRunnableFactory codeBuilderRunnableFactory) {
    this.fileLoader = fileLoader;
    this.inputAdapter = inputAdapter;
    this.outputAdapter = outputAdapter;
    this.executionType = executionType;
    this.compileList = compileList;
    this.tempDirectoryManager = tempDirectoryManager;
    this.lifecycleNotifier = lifecycleNotifier;
    this.contentManager = contentManager;
    this.systemExitHelper = systemExitHelper;
    this.codeBuilderRunnableFactory = codeBuilderRunnableFactory;
    this.executionInProgress = false;
  }

  /** Executes code synchronously, ensuring that pre- and post-execution steps are performed. */
  public void execute() {
    try {
      this.onPreExecute();
      try {
        // Run code builder
        this.executionInProgress = true;
        final CodeBuilderRunnable runnable =
            this.codeBuilderRunnableFactory.createCodeBuilderRunnable(
                this.fileLoader,
                this.outputAdapter,
                this.tempFolder,
                this.executionType,
                this.compileList);
        runnable.run();
      } catch (Throwable e) {
        // Catch any throwable here to ensure that onPostExecute() is always called
        LoggerUtils.logSevereException(e);
      }
      this.onPostExecute();
    } catch (InternalServerException e) {
      LoggerUtils.logSevereError(e);
    }
  }

  /**
   * Request the code execution to cleanup early (e.g. in the case of an impending timeout). Code
   * execution may still run, but this allows for post execution steps to still occur before the
   * environment is shut down.
   */
  public void requestEarlyExit() {
    if (!this.executionInProgress) {
      Logger.getLogger(MAIN_LOGGER)
          .warning("Early exit requested while execution not in progress.");
      return;
    }
    try {
      this.onPostExecute();
    } catch (InternalServerException e) {
      LoggerUtils.logSevereError(e);
    }
  }

  /**
   * Pre-execution steps: 1) Create GlobalProtocol, 2) create temporary folder, 3) Replace
   * System.in/out with custom in/out
   */
  private void onPreExecute() throws InternalServerException {
    // Create the Global Protocol instance
    GlobalProtocol.create(
        this.outputAdapter, this.inputAdapter, this.lifecycleNotifier, this.contentManager);

    // Create temp folder
    try {
      this.tempFolder = Files.createTempDirectory("tmpdir").toFile();
    } catch (IOException e) {
      throw new InternalServerException(InternalExceptionKey.INTERNAL_EXCEPTION, e);
    }

    // Save System in/out and replace with custom in/out
    this.systemInputStream = System.in;
    this.systemOutputStream = System.out;
    this.overrideInputStream =
        new InputRedirectionStream(GlobalProtocol.getInstance().getInputHandler());
    this.overrideOutputStream = new OutputPrintStream(this.outputAdapter);
    System.setOut(this.overrideOutputStream);
    System.setIn(this.overrideInputStream);
  }

  /**
   * Post-execution steps: 1) Notify listeners, 2) clean up global resources, 3) clear temporary
   * folder, 4) close custom in/out streams, 5) Replace System.in/out with original in/out, 6)
   * Destroy Global Protocol
   */
  private void onPostExecute() throws InternalServerException {
    if (!this.executionInProgress) {
      Logger.getLogger(MAIN_LOGGER)
          .warning("onPostExecute() called while execution not in progress.");
      return;
    }
    // Notify listeners
    this.lifecycleNotifier.onExecutionEnded();
    GlobalProtocol.getInstance().cleanUpResources();
    try {
      // Close custom input/output streams
      this.overrideInputStream.close();
      this.overrideOutputStream.close();
      // Clear temp folder
      this.tempDirectoryManager.cleanUpTempDirectory(this.tempFolder);
    } catch (IOException e) {
      // If there was an issue clearing the temp directory, this may be because too many files are
      // open. Force the JVM to quit in order to release the resources for the next use of the
      // container. Temporarily logging the exception for investigation purposes.
      LoggerUtils.logTrackingException(e);
      this.systemExitHelper.exit(TEMP_DIRECTORY_CLEANUP_ERROR_CODE);
    } finally {
      // Replace System in/out with original System in/out and destroy Global Protocol
      System.setIn(this.systemInputStream);
      System.setOut(this.systemOutputStream);
      GlobalProtocol.destroy();
      this.executionInProgress = false;
    }
  }
}
