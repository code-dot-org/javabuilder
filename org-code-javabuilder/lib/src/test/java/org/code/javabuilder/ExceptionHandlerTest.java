package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import org.code.protocol.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class ExceptionHandlerTest {

  private OutputAdapter outputAdapter;
  private SystemExitHelper systemExitHelper;
  private ArgumentCaptor<ClientMessage> messageCaptor;
  private ExceptionHandler unitUnderTest;
  private MockedStatic<LoggerUtils> loggerUtilsMockedStatic;

  @BeforeEach
  public void setUp() {
    outputAdapter = mock(OutputAdapter.class);
    systemExitHelper = mock(SystemExitHelper.class);
    messageCaptor = ArgumentCaptor.forClass(ClientMessage.class);
    unitUnderTest = new ExceptionHandler(outputAdapter, systemExitHelper);
    AWSMetricClient metricClient = mock(AWSMetricClient.class);
    JavabuilderContext.getInstance().register(MetricClient.class, metricClient);
    loggerUtilsMockedStatic = Mockito.mockStatic(LoggerUtils.class);
  }

  @AfterEach
  public void tearDown() {
    loggerUtilsMockedStatic.close();
  }

  @Test
  public void testFatalError() {
    final FatalError error = new FatalError(FatalErrorKey.LOW_DISK_SPACE);
    unitUnderTest.handle(error);

    // Should log, notify user, and exit
    loggerUtilsMockedStatic.verify(() -> LoggerUtils.logSevereError(any(), any(), any()));
    verify(outputAdapter).sendMessage(any(ClientMessage.class));
    verify(systemExitHelper).exit(error.getErrorCode());
  }

  @Test
  public void testInternalServerException() {
    final InternalServerException internal =
        new InternalServerException(InternalExceptionKey.INTERNAL_EXCEPTION);
    unitUnderTest.handle(internal);

    // Should log and notify user
    loggerUtilsMockedStatic.verify(() -> LoggerUtils.logSevereError(any(), any(), any()));
    verify(outputAdapter).sendMessage(any(ClientMessage.class));
  }

  @Test
  public void testInternalFacingException() {
    final InternalFacingException internal =
        new InternalFacingException("internal", new Exception());
    unitUnderTest.handle(internal);

    // Should log only
    loggerUtilsMockedStatic.verify(() -> LoggerUtils.logTrackingExceptionAsWarning(any()));
    verify(outputAdapter, never()).sendMessage(any(ClientMessage.class));
  }

  @Test
  public void testUserInitiatedException() {
    final UserInitiatedException userInitiated =
        new UserInitiatedException(UserInitiatedExceptionKey.COMPILER_ERROR);
    unitUnderTest.handle(userInitiated);

    // Should notify user only
    loggerUtilsMockedStatic.verify(() -> LoggerUtils.logSevereError(any(), any(), any()), never());
    verify(outputAdapter).sendMessage(any(ClientMessage.class));
  }

  @Test
  public void testUnknownException() {
    doNothing().when(outputAdapter).sendMessage(messageCaptor.capture());
    final IOException unknown = new IOException();
    unitUnderTest.handle(unknown);

    // Should log and notify user
    loggerUtilsMockedStatic.verify(() -> LoggerUtils.logSevereError(any()));
    verify(outputAdapter).sendMessage(any(ClientMessage.class));

    final ClientMessage message = messageCaptor.getValue();
    assertEquals(InternalExceptionKey.UNKNOWN_ERROR.toString(), message.getValue());
  }
}
