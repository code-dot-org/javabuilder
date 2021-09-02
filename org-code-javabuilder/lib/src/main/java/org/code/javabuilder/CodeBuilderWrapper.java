package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.util.logging.Logger;
import org.code.protocol.*;
import org.json.JSONObject;

/**
 * This creates a high-level wrapper that handles errors and exceptions in a way that can be
 * communicated to the user and logged by us. Note: All exceptions, runtime exceptions, and errors
 * are caught. This allows us to log our errors and communicate failure states to the user. This may
 * be thought of as similar to an HTTP error handler.
 */
public class CodeBuilderWrapper {
  private final ProjectFileLoader fileLoader;
  private final OutputAdapter outputAdapter;

  public CodeBuilderWrapper(ProjectFileLoader fileLoader, OutputAdapter outputAdapter) {
    this.fileLoader = fileLoader;
    this.outputAdapter = outputAdapter;
  }

  public void executeCodeBuilder() {
    try {
      UserProjectFiles userProjectFiles = fileLoader.loadFiles();
      try (CodeBuilder codeBuilder =
          new CodeBuilder(GlobalProtocol.getInstance(), userProjectFiles)) {
        codeBuilder.buildUserCode();
        codeBuilder.runUserCode();
      }
    } catch (InternalServerError | InternalServerRuntimeError e) {
      // The error was caused by us (essentially an HTTP 5xx error). Log it so we can fix it.
      JSONObject eventData = new JSONObject();
      eventData.put("exceptionMessage", e.getExceptionMessage());
      eventData.put("loggingString", e.getLoggingString());
      Logger.getLogger(MAIN_LOGGER).severe(eventData.toString());

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
      JSONObject eventData = new JSONObject();
      eventData.put("exceptionMessage", error.getExceptionMessage());
      eventData.put("loggingString", error.getLoggingString());
      Logger.getLogger(MAIN_LOGGER).severe(eventData.toString());

      // Additionally, these may have affected the user. For now, let's tell them about it.
      outputAdapter.sendMessage(error.getExceptionMessage());
    }
  }
}
