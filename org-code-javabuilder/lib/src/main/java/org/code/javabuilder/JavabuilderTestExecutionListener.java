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
  private static final String CHECK_MARK = "✔";
  private static final String HEAVY_X = "✖";

  private final OutputAdapter outputAdapter;

  public JavabuilderTestExecutionListener(OutputAdapter outputAdapter) {
    super();
    this.outputAdapter = outputAdapter;
  }

  /**
   * Called when the execution of the TestPlan has started, before any test has been executed. This
   * listener sends a message via the OutputAdapter to indicate that code is running.
   *
   * <p>See:
   * https://junit.org/junit5/docs/5.0.0/api/org/junit/platform/launcher/TestExecutionListener.html#testPlanExecutionStarted-org.junit.platform.launcher.TestPlan-
   *
   * @param testPlan describes the tree of tests that have been executed
   */
  public void testPlanExecutionStarted(TestPlan testPlan) {
    super.testPlanExecutionStarted(testPlan);
    this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.RUNNING));
  }

  /**
   * Called when the execution of a leaf or subtree of the TestPlan has finished, regardless of the
   * outcome. This listener publishes the test result and, if the test failed, the error message to
   * the OutputAdapter.
   *
   * <p>The format of the test result is: [icon] [test class name] > [test name] [test result]
   *
   * <p>for example:
   *
   * <p>✔ MyTestClass > myTest SUCCEEDED
   *
   * <p>✖ MyTestClass > myTest FAILED
   *
   * <p>failure message (MyTestClass:1)
   *
   * <p>See:
   * https://junit.org/junit5/docs/5.0.0/api/org/junit/platform/launcher/TestExecutionListener.html#executionFinished-org.junit.platform.launcher.TestIdentifier-org.junit.platform.engine.TestExecutionResult-
   *
   * @param testIdentifier the identifier of the finished test or container
   * @param testExecutionResult the (unaggregated) result of the execution for the supplied
   *     TestIdentifier
   */
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
    final String icon = status == TestExecutionResult.Status.SUCCESSFUL ? CHECK_MARK : HEAVY_X;
    final String resultMessage =
        String.format("%s %s > %s %s\n", icon, className, testIdentifier.getDisplayName(), status);
    this.outputAdapter.sendMessage(new SystemOutMessage(resultMessage));

    final Optional<Throwable> throwable = testExecutionResult.getThrowable();
    if (status != TestExecutionResult.Status.SUCCESSFUL && throwable.isPresent()) {
      this.outputAdapter.sendMessage(
          new SystemOutMessage(this.getErrorMessage(throwable.get(), className)));
    }
  }

  /**
   * Generates an error message for a failing test given the Throwable and class name associated
   * with the test.
   *
   * <p>If the test threw an {@link AssertionError} (i.e. an assertion in the test failed), then the
   * format of the error message is: "[error message] ([java class name]:[line number])"
   *
   * <p>If the test threw another exception, then format of the error message is: "[exception name]
   * thrown at [java class name]:[line number]"
   *
   * <p>For example:
   *
   * <p>assertion failed (MyTest:1)
   *
   * <p>FileNotFoundException thrown at MyTest:10
   *
   * @param throwable Throwable thrown by the test
   * @param className name of the test class
   * @return the error message
   */
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
   * error.
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
