package org.code.javabuilder;

import static org.code.javabuilder.LambdaErrorCodes.TEMP_DIRECTORY_CLEANUP_ERROR_CODE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;
import org.code.javabuilder.util.LambdaUtils;
import org.code.protocol.*;

/**
 * Manages the execution of code in a project. When asked to execute, it performs the necessary
 * pre-execution setup and starts code execution. When asked to shut down it ensures the necessary
 * post-execution cleanup steps are performed.
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
  private boolean isInitialized;

  static class CodeBuilderRunnableFactory {
    public CodeBuilderRunnable createCodeBuilderRunnable(
        ProjectFileLoader fileLoader,
        File tempFolder,
        ExecutionType executionType,
        List<String> compileList) {
      return new CodeBuilderRunnable(fileLoader, tempFolder, executionType, compileList);
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
  }

  /**
   * Performs pre-execution steps to initialize the execution environment and executes code
   * synchronously. Callers should expect that this method will throw if there are any exceptions
   * encountered during initialization or execution and handle them accordingly.
   *
   * @throws JavabuilderException or InternalFacingException if there is an exception during
   *     initialization or execution. Note that other runtime exceptions may also be thrown if
   *     encountered during execution.
   */
  public void execute() throws JavabuilderException, InternalFacingException {
    this.onPreExecute();
    final CodeBuilderRunnable runnable =
        this.codeBuilderRunnableFactory.createCodeBuilderRunnable(
            this.fileLoader, this.tempFolder, this.executionType, this.compileList);
    runnable.run();
  }

  /**
   * Shuts down the execution environment. If it has not already been initialized, this method does
   * nothing.
   */
  public void shutDown() {
    if (!this.isInitialized) {
      return;
    }
    this.onPostExecute();
  }

  /**
   * Pre-execution steps: 1) Create GlobalProtocol, 2) create temporary folder, 3) Replace
   * System.in/out with custom in/out
   */
  private void onPreExecute() throws InternalServerException {
    // Create the Global Protocol instance
    GlobalProtocol protocolInstance = new GlobalProtocol(
        this.outputAdapter, new InputHandler(this.inputAdapter), this.lifecycleNotifier, this.contentManager);
    JavabuilderContext.getInstance().register(GlobalProtocol.class, protocolInstance);

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
        new InputRedirectionStream(protocolInstance.getInputHandler());
    this.overrideOutputStream = new OutputPrintStream(this.outputAdapter);
    System.setOut(this.overrideOutputStream);
    System.setIn(this.overrideInputStream);
    this.isInitialized = true;
  }

  /**
   * Post-execution steps: 1) Notify listeners, 2) clean up global resources, 3) clear temporary
   * folder, 4) close custom in/out streams, 5) Replace System.in/out with original in/out, 6)
   * Destroy Global Protocol
   */
  private void onPostExecute() {
    // Notify user and listeners
    LambdaUtils.safelySendMessage(
        this.outputAdapter, new StatusMessage(StatusMessageKey.EXITED), false);
    this.lifecycleNotifier.onExecutionEnded();
    JavabuilderContext.getInstance().getGlobalProtocol().cleanUpResources();
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
      // Replace System in/out with original System in/out and reset JavabuilderContext
      System.setIn(this.systemInputStream);
      System.setOut(this.systemOutputStream);
      JavabuilderContext.getInstance().destroyAndReset();
      this.isInitialized = false;
    }
  }
}
