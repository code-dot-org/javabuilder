package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.code.protocol.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ExceptionHandlerTest {

  private OutputAdapter outputAdapter;
  private SystemExitHelper systemExitHelper;
  private Handler testHandler;
  private ArgumentCaptor<ClientMessage> messageCaptor;
  private ExceptionHandler unitUnderTest;

  @BeforeEach
  public void setUp() {
    outputAdapter = mock(OutputAdapter.class);
    systemExitHelper = mock(SystemExitHelper.class);
    testHandler = mock(Handler.class);
    messageCaptor = ArgumentCaptor.forClass(ClientMessage.class);
    unitUnderTest = new ExceptionHandler(outputAdapter, systemExitHelper);

    Logger.getLogger(MAIN_LOGGER).addHandler(testHandler);
    Logger.getLogger(MAIN_LOGGER).setUseParentHandlers(false);
    MetricClientManager.create(mock(MetricClient.class));
  }

  @AfterEach
  public void tearDown() {
    Logger.getLogger(MAIN_LOGGER).removeHandler(testHandler);
    Logger.getLogger(MAIN_LOGGER).setUseParentHandlers(true);
  }

  @Test
  public void testFatalError() {
    final FatalError error = new FatalError(FatalErrorKey.LOW_DISK_SPACE);
    unitUnderTest.handle(error);

    // Should log, notify user, and exit
    verify(testHandler).publish(any(LogRecord.class));
    verify(outputAdapter).sendMessage(any(ClientMessage.class));
    verify(systemExitHelper).exit(error.getErrorCode());
  }

  @Test
  public void testInternalServerException() {
    final InternalServerException internal =
        new InternalServerException(InternalExceptionKey.INTERNAL_EXCEPTION);
    unitUnderTest.handle(internal);

    // Should log and notify user
    verify(testHandler).publish(any(LogRecord.class));
    verify(outputAdapter).sendMessage(any(ClientMessage.class));
  }

  @Test
  public void testInternalFacingException() {
    final InternalFacingException internal =
        new InternalFacingException("internal", new Exception());
    unitUnderTest.handle(internal);

    // Should log only
    verify(testHandler).publish(any(LogRecord.class));
    verify(outputAdapter, never()).sendMessage(any(ClientMessage.class));
  }

  @Test
  public void testUserInitiatedException() {
    final UserInitiatedException userInitiated =
        new UserInitiatedException(UserInitiatedExceptionKey.COMPILER_ERROR);
    unitUnderTest.handle(userInitiated);

    // Should notify user only
    verify(testHandler, never()).publish(any(LogRecord.class));
    verify(outputAdapter).sendMessage(any(ClientMessage.class));
  }

  @Test
  public void testUnknownException() {
    doNothing().when(outputAdapter).sendMessage(messageCaptor.capture());
    final IOException unknown = new IOException();
    unitUnderTest.handle(unknown);

    // Should log and notify user
    verify(testHandler).publish(any(LogRecord.class));
    verify(outputAdapter).sendMessage(any(ClientMessage.class));

    final ClientMessage message = messageCaptor.getValue();
    assertEquals(InternalExceptionKey.UNKNOWN_ERROR.toString(), message.getValue());
  }
}
