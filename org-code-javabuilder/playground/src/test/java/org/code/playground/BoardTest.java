package org.code.playground;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.code.protocol.InputHandler;
import org.code.protocol.InputMessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class BoardTest {

  private PlaygroundMessageHandler playgroundMessageHandler;
  private InputHandler inputHandler;
  private ArgumentCaptor<PlaygroundMessage> messageCaptor;
  private Board unitUnderTest;

  @BeforeEach
  public void setUp() {
    playgroundMessageHandler = mock(PlaygroundMessageHandler.class);
    messageCaptor = ArgumentCaptor.forClass(PlaygroundMessage.class);
    inputHandler = mock(InputHandler.class);
    unitUnderTest = new Board(playgroundMessageHandler, inputHandler);
  }

  @Test
  public void testGetWidthReturnsDefaultWidth() {
    assertEquals(400, unitUnderTest.getWidth());
  }

  @Test
  public void testGetHeightReturnsDefaultHeight() {
    assertEquals(400, unitUnderTest.getHeight());
  }

  @Test
  public void testRunSendsMessageAndWaitsForInput() throws PlaygroundException {
    // Need to make sure exit() is called so run() terminates
    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenAnswer(
            invocation -> {
              unitUnderTest.exit();
              return "id";
            });

    unitUnderTest.run();

    verify(playgroundMessageHandler, times(2)).sendMessage(messageCaptor.capture());
    assertEquals(
        PlaygroundSignalKey.RUN.toString(), messageCaptor.getAllValues().get(0).getValue());
    verify(inputHandler).getNextMessageForType(InputMessageType.PLAYGROUND);
  }

  @Test
  public void testRunThrowsExceptionIfCalledTwice() throws PlaygroundException {
    // Need to make sure exit() is called so run() terminates
    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenAnswer(
            invocation -> {
              unitUnderTest.exit();
              return "id";
            });

    unitUnderTest.run();
    final PlaygroundException e =
        assertThrows(PlaygroundException.class, () -> unitUnderTest.run());
    assertEquals(PlaygroundExceptionKeys.PLAYGROUND_RUNNING.toString(), e.getMessage());
  }

  @Test
  public void testExitSendsExitMessage() throws PlaygroundException {
    // Ensure that exit() is called while running to avoid exception
    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenAnswer(
            invocation -> {
              unitUnderTest.exit();
              return "id";
            });
    unitUnderTest.run();

    verify(playgroundMessageHandler, times(2)).sendMessage(messageCaptor.capture());
    assertEquals(
        PlaygroundSignalKey.EXIT.toString(), messageCaptor.getAllValues().get(1).getValue());
  }

  @Test
  public void testExitThrowsExceptionIfNotRunning() {
    final PlaygroundException e =
        assertThrows(PlaygroundException.class, () -> unitUnderTest.exit());
    assertEquals(PlaygroundExceptionKeys.PLAYGROUND_NOT_RUNNING.toString(), e.getMessage());
  }
}
