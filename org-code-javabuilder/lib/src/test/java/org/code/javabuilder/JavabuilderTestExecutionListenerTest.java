package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.util.Optional;
import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;
import org.code.protocol.OutputAdapter;
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

  @BeforeEach
  public void setUp() {
    outputAdapter = mock(OutputAdapter.class);
    testPlan = mock(TestPlan.class);
    testIdentifier = mock(TestIdentifier.class);
    testExecutionResult = mock(TestExecutionResult.class);
    messageCaptor = ArgumentCaptor.forClass(ClientMessage.class);
    unitUnderTest = new JavabuilderTestExecutionListener(outputAdapter);
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
    final String displayName = "displayName";
    final String className = "myClass";
    final MethodSource methodSource = MethodSource.from(className, "method");

    when(testIdentifier.getDisplayName()).thenReturn(displayName);
    when(testIdentifier.isTest()).thenReturn(true);
    when(testIdentifier.getSource()).thenReturn(Optional.of(methodSource));
    when(testExecutionResult.getStatus()).thenReturn(TestExecutionResult.Status.SUCCESSFUL);

    // Need to call testPlanExecutionStarted() to prevent NullPointerException in
    // SummaryGeneratingListener
    unitUnderTest.testPlanExecutionStarted(testPlan);
    unitUnderTest.executionFinished(testIdentifier, testExecutionResult);

    verify(outputAdapter, times(1)).sendMessage(messageCaptor.capture());

    final ClientMessage message = messageCaptor.getAllValues().get(0);
    assertEquals(ClientMessageType.TEST_RESULT, message.getType());
    assertTrue(message.getValue().contains(displayName));
    assertTrue(message.getValue().contains(className));
    assertTrue(message.getValue().contains(TestExecutionResult.Status.SUCCESSFUL.toString()));
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

    when(testIdentifier.getDisplayName()).thenReturn("displayName");
    when(testIdentifier.isTest()).thenReturn(true);
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

    // Error message should contain throwable message, file name, and line number
    assertTrue(message.getValue().contains(errorMessage));
    assertTrue(message.getValue().contains(fileName));
    assertTrue(message.getValue().contains(Integer.toString(lineNumber)));
  }

  @Test
  public void testExecutionFinishedPrintsExceptionIfThrown() {
    final String exceptionMessage = "exceptionMessage";
    final Throwable error = new FileNotFoundException(exceptionMessage);

    when(testIdentifier.getDisplayName()).thenReturn("displayName");
    when(testIdentifier.isTest()).thenReturn(true);
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

    // Since the test threw an exception, only the exception name should be included
    assertTrue(message.getValue().contains(error.getClass().getSimpleName()));
    assertFalse(message.getValue().contains(exceptionMessage));
  }
}
