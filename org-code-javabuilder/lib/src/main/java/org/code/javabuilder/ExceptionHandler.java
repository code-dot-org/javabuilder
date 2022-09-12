package org.code.javabuilder;

import org.code.javabuilder.util.LambdaUtils;
import org.code.protocol.*;

/**
 * This is a top-level error/exception handler that handles errors and exceptions in a way that can
 * be communicated to the user and logged by us. Note: All exceptions, runtime exceptions, and
 * errors are caught. This allows us to log our errors and communicate failure states to the user.
 * This may be thought of as similar to an HTTP error handler.
 */
public class ExceptionHandler {
  private final OutputAdapter outputAdapter;
  private final SystemExitHelper systemExitHelper;

  public ExceptionHandler(OutputAdapter outputAdapter, SystemExitHelper systemExitHelper) {
    this.outputAdapter = outputAdapter;
    this.systemExitHelper = systemExitHelper;
  }

  public void handle(Throwable e) {
    // Fatal errors are non-recoverable: notify user and shut down runtime.
    if (e instanceof FatalError) {
      final FatalError error = (FatalError) e;
      LoggerUtils.logSevereError(
          error.getExceptionMessage(), error.getLoggingString(), e.getCause());
      LambdaUtils.safelySendMessage(this.outputAdapter, error.getExceptionMessage(), true);
      this.systemExitHelper.exit(error.getErrorCode());
      return;
    }

    // We should also exit on an OutOfMemoryError.
    // This means something is wrong with our memory usage
    // and is not likely to be recoverable.
    if (e instanceof OutOfMemoryError) {
      final InternalServerException error =
          new InternalServerException(InternalExceptionKey.UNKNOWN_ERROR, e);
      LoggerUtils.logSevereError(error);
      LambdaUtils.safelySendMessage(this.outputAdapter, error.getExceptionMessage(), true);
      this.systemExitHelper.exit(LambdaErrorCodes.OUT_OF_MEMORY_ERROR_CODE);
      return;
    }

    // Internal server exceptions are caused by us (essentially an HTTP 5xx error). Log and notify
    // the user.
    if (e instanceof InternalServerException || e instanceof InternalServerRuntimeException) {
      final JavabuilderThrowableProtocol throwable = (JavabuilderThrowableProtocol) e;
      LoggerUtils.logSevereError(
          throwable.getExceptionMessage(), throwable.getLoggingString(), e.getCause());
      LambdaUtils.safelySendMessage(this.outputAdapter, throwable.getExceptionMessage(), true);
      return;
    }

    // Internal facing exceptions are caused by us (essentially an HTTP 5xx error), but don't affect
    // the user. Log only.
    if (e instanceof InternalFacingException || e instanceof InternalFacingRuntimeException) {
      LoggerUtils.logTrackingExceptionAsWarning(e);
      return;
    }

    // Any other Javabuilder exceptions should be caused by the user. Notify them.
    if (e instanceof JavabuilderException || e instanceof JavabuilderRuntimeException) {
      LambdaUtils.safelySendMessage(
          this.outputAdapter, ((JavabuilderThrowableProtocol) e).getExceptionMessage(), true);
      return;
    }

    // Finally, handle any errors or exceptions we didn't catch. These may have been caused by the
    // JVM, our own setup, or many other unknowns. Log them so we can fix them, and notify the user.
    final InternalServerException error =
        new InternalServerException(InternalExceptionKey.UNKNOWN_ERROR, e);
    LoggerUtils.logSevereError(error);
    LambdaUtils.safelySendMessage(this.outputAdapter, error.getExceptionMessage(), true);
  }
}
