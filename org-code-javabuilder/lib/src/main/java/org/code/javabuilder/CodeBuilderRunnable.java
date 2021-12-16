package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import org.code.protocol.*;

/**
 * This creates a high-level wrapper that handles errors and exceptions in a way that can be
 * communicated to the user and logged by us. Note: All exceptions, runtime exceptions, and errors
 * are caught. This allows us to log our errors and communicate failure states to the user. This may
 * be thought of as similar to an HTTP error handler.
 */
public class CodeBuilderRunnable implements Runnable {
  private final ProjectFileLoader fileLoader;
  private final OutputAdapter outputAdapter;
  private final File tempFolder;
  private final ExecutionType executionType;
  private final List<String> compileList;

  public CodeBuilderRunnable(
      ProjectFileLoader fileLoader,
      OutputAdapter outputAdapter,
      File tempFolder,
      ExecutionType executionType,
      List<String> compileList) {
    this.fileLoader = fileLoader;
    this.outputAdapter = outputAdapter;
    this.tempFolder = tempFolder;
    this.executionType = executionType;
    this.compileList = compileList;
  }

  @Override
  public void run() {
    this.executeCodeBuilder();
  }

  private void executeCodeBuilder() {
    try {
      UserProjectFiles userProjectFiles = fileLoader.loadFiles();
      try (CodeBuilder codeBuilder =
          new CodeBuilder(GlobalProtocol.getInstance(), userProjectFiles, this.tempFolder)) {

        switch (this.executionType) {
          case COMPILE_ONLY:
            codeBuilder.buildUserCode(this.compileList);
            break;
          case RUN:
            codeBuilder.buildAllUserCode();
            codeBuilder.runUserCode();
            break;
          case TEST:
            codeBuilder.buildAllUserCode();
            codeBuilder.runUserTests();
            break;
        }
      }
    } catch (InternalServerError | InternalServerRuntimeError e) {
      if (e.getCause().getClass().equals(InterruptedException.class)) {
        // Interrupted Exception is thrown if the code was manually shut down.
        // Ignore this exception
        Logger.getLogger(MAIN_LOGGER).info("Catching interruption in execution thread.");
        return;
      }
      if (e.getMessage().equals(InternalErrorKey.CONNECTION_TERMINATED.toString())) {
        // The connection was terminated while trying to send or receive a message. We no longer
        // have a connection to the user, so we need to log a warning internally and exit.
        Logger.getLogger(MAIN_LOGGER).warning(e.getLoggingString());
        return;
      }
      // The error was caused by us (essentially an HTTP 5xx error). Log it so we can fix it.
      LoggerUtils.logError(e.getExceptionMessage(), e.getLoggingString(), e.getCause());

      // The error affected the user. Tell them about it.
      outputAdapter.sendMessage(e.getExceptionMessage());
    } catch (JavabuilderException | JavabuilderRuntimeException e) {
      // The error affected the user and was caused by them. Tell them about it.
      outputAdapter.sendMessage(e.getExceptionMessage());
    } catch (InternalFacingException e) {
      // The error was caused by us (essentially an HTTP 5xx error), but doesn't affect the user.
      // Log it so we can fix it.
      Logger.getLogger(MAIN_LOGGER).warning(e.getLoggingString());
    } catch (Throwable e) {
      // Note: We intentionally catch _all_ Throwables here, including Errors. This is so we can log
      // the errors and communicate the error state to the user before exiting the program.

      // Wrap this in our error type so we can log it and tell the user.
      InternalServerError error = new InternalServerError(InternalErrorKey.UNKNOWN_ERROR, e);

      // Errors we didn't catch. These may have been caused by the JVM, our own setup, or many other
      // unknowns. Log them so we can fix them.
      LoggerUtils.logError(error);

      // Additionally, these may have affected the user. For now, let's tell them about it.
      outputAdapter.sendMessage(error.getExceptionMessage());
    }
    // Send exit message if we haven't already returned
    this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.EXITED));
  }
}
