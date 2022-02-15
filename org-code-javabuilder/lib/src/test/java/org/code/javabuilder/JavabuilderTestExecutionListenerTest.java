package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.util.Optional;
import org.code.protocol.*;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.mockito.ArgumentCaptor;

public class JavabuilderTestExecutionListenerTest {

  private OutputAdapter outputAdapter;
  private TestPlan testPlan;
  private TestIdentifier testIdentifier;
  private TestExecutionResult testExecutionResult;
  private ArgumentCaptor<ClientMessage> messageCaptor;
  private JavabuilderTestExecutionListener unitUnderTest;
  private final String displayName = "displayName";
  private final String classDisplayName = "Class Display Name";

  @BeforeEach
  public void setUp() {
    outputAdapter = mock(OutputAdapter.class);
    testPlan = mock(TestPlan.class);
    testIdentifier = mock(TestIdentifier.class);
    testExecutionResult = mock(TestExecutionResult.class);
    messageCaptor = ArgumentCaptor.forClass(ClientMessage.class);
    unitUnderTest = new JavabuilderTestExecutionListener(outputAdapter, false);
    TestIdentifier classTestIdentifier = mock(TestIdentifier.class);

    when(testIdentifier.getDisplayName()).thenReturn(displayName);
    when(testIdentifier.isTest()).thenReturn(true);
    when(classTestIdentifier.getDisplayName()).thenReturn(classDisplayName);
    when(testPlan.getParent(testIdentifier)).thenReturn(Optional.of(classTestIdentifier));
  }

  @Test
  public void testExecutionFinishedDoesNothingIfNotTest() {
    when(testIdentifier.isTest()).thenReturn(false);
    when(testExecutionResult.getStatus()).thenReturn(TestExecutionResult.Status.SUCCESSFUL);

    unitUnderTest.executionFinished(testIdentifier, testExecutionResult);

    verify(outputAdapter, never()).sendMessage(any(ClientMessage.class));
  }

  @Test
  public void testExecutionFinishedSendsStatusMessage() {
    final String className = "myClass";
    final MethodSource methodSource = MethodSource.from(className, "method");

    when(testIdentifier.getSource()).thenReturn(Optional.of(methodSource));
    when(testExecutionResult.getStatus()).thenReturn(TestExecutionResult.Status.SUCCESSFUL);

    // Need to call testPlanExecutionStarted() to prevent NullPointerException in
    // SummaryGeneratingListener
    unitUnderTest.testPlanExecutionStarted(testPlan);
    unitUnderTest.executionFinished(testIdentifier, testExecutionResult);

    verify(outputAdapter, times(1)).sendMessage(messageCaptor.capture());

    final ClientMessage message = messageCaptor.getAllValues().get(0);
    assertEquals(ClientMessageType.TEST_RESULT, message.getType());

    JSONObject messageDetail = message.getDetail();
    assertTrue(messageDetail.getString(ClientMessageDetailKeys.METHOD_NAME).equals(displayName));
    assertTrue(
        messageDetail.getString(ClientMessageDetailKeys.CLASS_NAME).equals(classDisplayName));
    assertTrue(
        messageDetail
            .getString(ClientMessageDetailKeys.STATUS)
            .equals(TestExecutionResult.Status.SUCCESSFUL.toString()));
  }

  @Test
  public void testExecutionFinishedSendsErrorMessageIfTestFails() {
    final String className = "myClass";
    final MethodSource methodSource = MethodSource.from(className, "method");
    final String errorMessage = "errorMessage";
    final String fileName = className + ".java";
    final int lineNumber = 10;

    // Create a mock stack trace with two items, the second containing the relevant line
    final StackTraceElement[] stackTrace = {
      new StackTraceElement("otherclass", "method", "otherfile", 1),
      new StackTraceElement(className, "method", fileName, lineNumber)
    };
    final Throwable error = new AssertionError(errorMessage);
    error.setStackTrace(stackTrace);

    when(testIdentifier.getSource()).thenReturn(Optional.of(methodSource));
    when(testExecutionResult.getStatus()).thenReturn(TestExecutionResult.Status.FAILED);
    when(testExecutionResult.getThrowable()).thenReturn(Optional.of(error));

    // Need to call testPlanExecutionStarted() to prevent NullPointerException in
    // SummaryGeneratingListener
    unitUnderTest.testPlanExecutionStarted(testPlan);
    unitUnderTest.executionFinished(testIdentifier, testExecutionResult);

    // 2 calls: 1) result, 2) error details
    verify(outputAdapter, times(2)).sendMessage(messageCaptor.capture());

    final ClientMessage message = messageCaptor.getAllValues().get(1);
    assertEquals(ClientMessageType.TEST_RESULT, message.getType());

    // Details should contain throwable message, file name, and line number
    JSONObject messageDetail = message.getDetail();
    assertTrue(
        messageDetail.getString(ClientMessageDetailKeys.ASSERTION_ERROR).equals(errorMessage));
    assertTrue(messageDetail.getString(ClientMessageDetailKeys.FILE_NAME).equals(fileName));
    assertTrue(
        messageDetail
            .getString(ClientMessageDetailKeys.ERROR_LINE)
            .equals(Integer.toString(lineNumber)));
  }

  @Test
  public void testExecutionFinishedSendsTypeForJavabuilderException() {
    final String className = "myClass";
    final MethodSource methodSource = MethodSource.from(className, "method");
    final String fileName = className + ".java";
    final int lineNumber = 10;

    // Create a mock stack trace with two items, the second containing the relevant line
    final StackTraceElement[] stackTrace = {
      new StackTraceElement("otherclass", "method", "otherfile", 1),
      new StackTraceElement(className, "method", fileName, lineNumber)
    };
    final Throwable error =
        new InternalServerRuntimeError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION);
    error.setStackTrace(stackTrace);

    when(testIdentifier.getSource()).thenReturn(Optional.of(methodSource));
    when(testExecutionResult.getStatus()).thenReturn(TestExecutionResult.Status.FAILED);
    when(testExecutionResult.getThrowable()).thenReturn(Optional.of(error));

    // Need to call testPlanExecutionStarted() to prevent NullPointerException in
    // SummaryGeneratingListener
    unitUnderTest.testPlanExecutionStarted(testPlan);
    unitUnderTest.executionFinished(testIdentifier, testExecutionResult);

    // 2 calls: 1) result, 2) error details
    verify(outputAdapter, times(2)).sendMessage(messageCaptor.capture());

    final ClientMessage message = messageCaptor.getAllValues().get(1);
    assertEquals(ClientMessageType.TEST_RESULT, message.getType());

    // Details should contain runtime exception type, file name, and line number
    JSONObject messageDetail = message.getDetail();
    assertTrue(
        messageDetail
            .getString(ClientMessageDetailKeys.TYPE)
            .equals(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION.toString()));
    assertTrue(messageDetail.getString(ClientMessageDetailKeys.FILE_NAME).equals(fileName));
    assertTrue(
        messageDetail
            .getString(ClientMessageDetailKeys.ERROR_LINE)
            .equals(Integer.toString(lineNumber)));
  }

  @Test
  public void testExecutionFinishedPrintsExceptionIfThrown() {
    final String exceptionMessage = "exceptionMessage";
    final Throwable error = new FileNotFoundException(exceptionMessage);

    when(testIdentifier.getSource())
        .thenReturn(Optional.of(MethodSource.from("myClass", "method")));
    when(testExecutionResult.getStatus()).thenReturn(TestExecutionResult.Status.FAILED);
    when(testExecutionResult.getThrowable()).thenReturn(Optional.of(error));

    // Need to call testPlanExecutionStarted() to prevent NullPointerException in
    // SummaryGeneratingListener
    unitUnderTest.testPlanExecutionStarted(testPlan);
    unitUnderTest.executionFinished(testIdentifier, testExecutionResult);

    // 2 calls: 1) result, 2) error details
    verify(outputAdapter, times(2)).sendMessage(messageCaptor.capture());

    final ClientMessage message = messageCaptor.getAllValues().get(1);
    assertEquals(ClientMessageType.TEST_RESULT, message.getType());

    // Since the test threw a non-internal exception, the exception name should be sent
    JSONObject messageDetail = message.getDetail();
    assertTrue(
        messageDetail
            .getString(ClientMessageDetailKeys.EXCEPTION_NAME)
            .equals(error.getClass().getSimpleName()));
  }
}
