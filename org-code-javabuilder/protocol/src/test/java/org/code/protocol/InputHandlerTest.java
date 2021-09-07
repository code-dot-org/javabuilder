package org.code.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InputHandlerTest {

  private InputAdapter inputAdapter;
  private InputHandler unitUnderTest;

  @BeforeEach
  public void setUp() {
    inputAdapter = mock(InputAdapter.class);
    unitUnderTest = new InputHandler(inputAdapter);
  }

  @Test
  public void testGetsNextMessageForTypeCorrectly() {
    final String testMessage = "test message";
    when(inputAdapter.getNextMessage())
        .thenReturn(createJsonMessage(InputMessageType.SYSTEM_IN.name(), testMessage));
    assertEquals(testMessage, unitUnderTest.getNextMessageForType(InputMessageType.SYSTEM_IN));
  }

  @Test
  public void testQueuesMessageIfNotOfRequestedType() {
    final String inputMessage1 = "system in 1";
    final String inputMessage2 = "system in 2";
    final String playgroundMessage = "playground message";

    when(inputAdapter.getNextMessage())
        .thenReturn(createJsonMessage(InputMessageType.SYSTEM_IN.name(), inputMessage1))
        .thenReturn(createJsonMessage(InputMessageType.SYSTEM_IN.name(), inputMessage2))
        .thenReturn(createJsonMessage(InputMessageType.PLAYGROUND.name(), playgroundMessage))
        .thenReturn(createJsonMessage(InputMessageType.PLAYGROUND.name(), "other"));

    assertEquals(
        playgroundMessage, unitUnderTest.getNextMessageForType(InputMessageType.PLAYGROUND));
    // Should have called input adapter 3 times (until first PLAYGROUND message was received)
    verify(inputAdapter, times(3)).getNextMessage();

    reset(inputAdapter);

    assertEquals(inputMessage1, unitUnderTest.getNextMessageForType(InputMessageType.SYSTEM_IN));
    assertEquals(inputMessage2, unitUnderTest.getNextMessageForType(InputMessageType.SYSTEM_IN));
    // SYSTEM_IN messages should have already been queued so input adapter should not be called
    verify(inputAdapter, never()).getNextMessage();
  }

  @Test
  public void testThrowsExceptionIfMessageIsNotJson() {
    final String testMessage = "not json";
    when(inputAdapter.getNextMessage()).thenReturn(testMessage);
    Exception e =
        assertThrows(
            InternalServerRuntimeError.class,
            () -> unitUnderTest.getNextMessageForType(InputMessageType.SYSTEM_IN));
    assertEquals(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION.name(), e.getMessage());
  }

  @Test
  public void testThrowsExceptionIfMessageHasInvalidType() {
    final String testMessage = createJsonMessage("invalidType", "invalidMessage");
    when(inputAdapter.getNextMessage()).thenReturn(testMessage);
    Exception e =
        assertThrows(
            InternalServerRuntimeError.class,
            () -> unitUnderTest.getNextMessageForType(InputMessageType.SYSTEM_IN));
    assertEquals(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION.name(), e.getMessage());
  }

  @Test
  public void testThrowsExceptionIfMessageTextIsMissing() {
    final String testMessage =
        new JSONObject(Map.of("messageType", InputMessageType.SYSTEM_IN.name())).toString();
    when(inputAdapter.getNextMessage()).thenReturn(testMessage);
    Exception e =
        assertThrows(
            InternalServerRuntimeError.class,
            () -> unitUnderTest.getNextMessageForType(InputMessageType.SYSTEM_IN));
    assertEquals(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION.name(), e.getMessage());
  }

  private String createJsonMessage(String messageType, String message) {
    return new JSONObject(Map.of("messageType", messageType, "message", message)).toString();
  }
}
