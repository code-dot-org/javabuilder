package org.code.javabuilder;

import java.util.Optional;
import org.code.protocol.OutputAdapter;
import org.code.protocol.StatusMessage;
import org.code.protocol.StatusMessageKey;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

public class JavabuilderTestExecutionListener extends SummaryGeneratingListener {
  private final OutputAdapter outputAdapter;

  public JavabuilderTestExecutionListener(OutputAdapter outputAdapter) {
    super();
    this.outputAdapter = outputAdapter;
  }

  public void testPlanExecutionStarted(TestPlan testPlan) {
    super.testPlanExecutionStarted(testPlan);
    this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.RUNNING));
  }

  public void executionFinished(
      TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
    super.executionFinished(testIdentifier, testExecutionResult);

    if (!testIdentifier.isTest()) {
      return;
    }

    String className = "";
    final Optional<TestSource> testSource = testIdentifier.getSource();
    if (testSource.isPresent() && testSource.get() instanceof MethodSource) {
      className = ((MethodSource) testSource.get()).getClassName();
    }

    final TestExecutionResult.Status status = testExecutionResult.getStatus();
    final String resultMessage =
        String.format("%s > %s %s\n", className, testIdentifier.getDisplayName(), status);
    this.outputAdapter.sendMessage(new SystemOutMessage(resultMessage));

    final Optional<Throwable> throwable = testExecutionResult.getThrowable();
    if (status != TestExecutionResult.Status.SUCCESSFUL && throwable.isPresent()) {
      this.outputAdapter.sendMessage(
          new SystemOutMessage(this.getErrorMessage(throwable.get(), className)));
    }
  }

  private String getErrorMessage(Throwable throwable, String className) {
    final String errorLine = this.errorLine(throwable.getStackTrace(), className);
    final String errorMessage;

    // If there was an assertion failure, print the failure message. If an exception was thrown,
    // print the exception name.
    if (throwable instanceof AssertionError) {
      errorMessage = String.format("\t%s (%s)\n", throwable.getMessage(), errorLine);
    } else {
      errorMessage =
          String.format("\t%s thrown at %s\n", throwable.getClass().getSimpleName(), errorLine);
    }

    return errorMessage;
  }

  /**
   * Finds the stack trace element containing the class name and returns the line number of the
   * error
   *
   * @param stackTrace array of stack trace elements
   * @param className name of the test class
   * @return test file name and line number of the error
   */
  private String errorLine(StackTraceElement[] stackTrace, String className) {
    for (StackTraceElement stackTraceElement : stackTrace) {
      if (stackTraceElement.getClassName().equals(className)) {
        return stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber();
      }
    }
    return null;
  }
}
