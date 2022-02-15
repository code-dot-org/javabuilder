package org.code.javabuilder;

import java.util.HashMap;
import java.util.Optional;
import org.code.protocol.*;
import org.code.validation.support.UserTestResultMessage;
import org.code.validation.support.UserTestResultSignalKey;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

/**
 * A {@link org.junit.platform.launcher.TestExecutionListener} that posts test status and results to
 * the provided {@link OutputAdapter}. Since this class only implements a subset of
 * TestExecutionListener methods, it extends {@link SummaryGeneratingListener} and defers any
 * additional functionality to it.
 *
 * <p>See:
 * https://junit.org/junit5/docs/5.0.0/api/org/junit/platform/launcher/listeners/SummaryGeneratingListener.html
 * https://junit.org/junit5/docs/5.0.0/api/org/junit/platform/launcher/TestExecutionListener.html
 */
public class JavabuilderTestExecutionListener extends SummaryGeneratingListener {
  private final OutputAdapter outputAdapter;
  private final boolean isValidation;
  private TestPlan testPlan;

  public JavabuilderTestExecutionListener(OutputAdapter outputAdapter, boolean isValidation) {
    super();
    this.outputAdapter = outputAdapter;
    this.isValidation = isValidation;
  }

  /**
   * Called when the execution of the TestPlan has started, before any test has been executed.
   *
   * <p>See:
   * https://junit.org/junit5/docs/5.0.0/api/org/junit/platform/launcher/listeners/SummaryGeneratingListener.html#testPlanExecutionStarted-org.junit.platform.launcher.TestPlan-
   *
   * @param testPlan describes the tree of tests about to be executed
   */
  @Override
  public void testPlanExecutionStarted(TestPlan testPlan) {
    super.testPlanExecutionStarted(testPlan);
    this.testPlan = testPlan;
  }

  /**
   * Called when the execution of a leaf or subtree of the TestPlan has finished, regardless of the
   * outcome. This listener publishes the test result and, if the test failed, the error message to
   * the OutputAdapter.
   *
   * <p>The test result will be sent as a set of keys and values. The basic result message will be
   * sent as: { status: one of SUCCESSFUL/FAILED/ABORTED, className: display name of test class,
   * methodName: display name of method, isValidation: true/false }
   *
   * <p>Failed tests will also send an additional detail message with error details. See
   * getErrorMessageDetails for more information.
   *
   * <p>See:
   * https://junit.org/junit5/docs/5.0.0/api/org/junit/platform/launcher/TestExecutionListener.html#executionFinished-org.junit.platform.launcher.TestIdentifier-org.junit.platform.engine.TestExecutionResult-
   *
   * @param testIdentifier the identifier of the finished test or container
   * @param testExecutionResult the (unaggregated) result of the execution for the supplied
   *     TestIdentifier
   */
  @Override
  public void executionFinished(
      TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
    super.executionFinished(testIdentifier, testExecutionResult);

    if (!testIdentifier.isTest()) {
      return;
    }

    String classDisplayName = "";
    final Optional<TestIdentifier> parent = this.testPlan.getParent(testIdentifier);
    if (parent.isPresent()) {
      // Parent will be the containing class for the test.
      classDisplayName = parent.get().getDisplayName();
    }

    // name of class, used for generating error messages
    String className = "";
    final Optional<TestSource> testSource = testIdentifier.getSource();
    if (testSource.isPresent() && testSource.get() instanceof MethodSource) {
      className = ((MethodSource) testSource.get()).getClassName();
    }

    final TestExecutionResult.Status status = testExecutionResult.getStatus();
    HashMap<String, String> statusMessageDetails = new HashMap<>();
    statusMessageDetails.put(ClientMessageDetailKeys.STATUS, status.toString());
    statusMessageDetails.put(ClientMessageDetailKeys.CLASS_NAME, classDisplayName);
    statusMessageDetails.put(ClientMessageDetailKeys.METHOD_NAME, testIdentifier.getDisplayName());
    statusMessageDetails.put(
        ClientMessageDetailKeys.IS_VALIDATION, String.valueOf(this.isValidation));
    this.outputAdapter.sendMessage(
        new UserTestResultMessage(UserTestResultSignalKey.TEST_STATUS, statusMessageDetails));

    final Optional<Throwable> throwable = testExecutionResult.getThrowable();
    if (status != TestExecutionResult.Status.SUCCESSFUL && throwable.isPresent()) {
      this.outputAdapter.sendMessage(
          new UserTestResultMessage(
              UserTestResultSignalKey.STATUS_DETAILS,
              this.getErrorMessageDetails(throwable.get(), className)));
    }
  }

  /**
   * Generates error message details map for a failing test given the Throwable and class name
   * associated with the test. For all cases described below, the file name and line number where
   * the throwable was thrown will be included in the details.
   *
   * <p>If the test threw an {@link AssertionError} (i.e. an assertion in the test failed), then the
   * error message from the assertion will be included with key assertionError.
   *
   * <p>If the test threw a JavabuilderRuntimeError, the error details will include custom exception
   * details generated by JavabuilderRuntimeError.
   *
   * <p>If the test threw any other exception, the error details will include the exception name and
   * stack trace, with keys exceptionName and stackTrace.
   *
   * @param throwable Throwable thrown by the test
   * @param className name of the test class
   * @return the error message
   */
  private HashMap<String, String> getErrorMessageDetails(Throwable throwable, String className) {
    HashMap<String, String> errorDetails = new HashMap<>();
    this.setErrorLineDetails(throwable.getStackTrace(), className, errorDetails);

    // If there was an assertion failure, send on the message.
    // If a JavabuilderRuntimeException was thrown, send the exception details in a hashmap.
    // If another exception was thrown, send the exception name.
    if (throwable instanceof AssertionError) {
      errorDetails.put(ClientMessageDetailKeys.ASSERTION_ERROR, throwable.getMessage());
    } else if (throwable instanceof JavabuilderRuntimeException) {
      JavabuilderRuntimeException exception = (JavabuilderRuntimeException) throwable;
      errorDetails.putAll(exception.getExceptionDetails());
      errorDetails.put(ClientMessageDetailKeys.TYPE, exception.getMessage());
    } else {
      errorDetails.put(
          ClientMessageDetailKeys.EXCEPTION_NAME, this.getThrowableDisplayName(throwable));
      errorDetails.put(
          ClientMessageDetailKeys.STACK_TRACE,
          JavabuilderThrowableMessageUtils.getUserFacingStackTraceString(throwable));
    }

    return errorDetails;
  }

  /**
   * Finds the stack trace element containing the class name and returns the line number of the
   * error.
   *
   * @param stackTrace array of stack trace elements
   * @param className name of the test class
   */
  private void setErrorLineDetails(
      StackTraceElement[] stackTrace, String className, HashMap<String, String> errorDetails) {
    for (StackTraceElement stackTraceElement : stackTrace) {
      if (stackTraceElement.getClassName().equals(className)) {
        errorDetails.put(ClientMessageDetailKeys.FILE_NAME, stackTraceElement.getFileName());
        errorDetails.put(
            ClientMessageDetailKeys.ERROR_LINE, String.valueOf(stackTraceElement.getLineNumber()));
      }
    }
  }

  private String getThrowableDisplayName(Throwable throwable) {
    // A NoClassDefFoundError occurs when a user uses a disallowed class. We should
    // instead show a ClassNotFoundException.
    if (throwable instanceof NoClassDefFoundError) {
      return ClassNotFoundException.class.getSimpleName();
    } else {
      return throwable.getClass().getSimpleName();
    }
  }
}
