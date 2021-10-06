package org.code.playground;

import static org.mockito.Mockito.*;

import org.code.protocol.OutputAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlaygroundMessageHandlerTest {

  private OutputAdapter outputAdapter;
  private PlaygroundMessageHandler unitUnderTest;

  @BeforeEach
  public void setUp() {
    outputAdapter = mock(OutputAdapter.class);
    unitUnderTest = new PlaygroundMessageHandler(outputAdapter);
  }

  @Test
  public void testSendMessageDoesNotCallOutputAdapter() {
    final PlaygroundMessage message = new PlaygroundMessage(PlaygroundSignalKey.RUN);

    unitUnderTest.sendMessage(message);

    verify(outputAdapter, times(0)).sendMessage(any());
  }

  @Test
  public void testSendBatchedMessagesCallsOutputAdapter() {
    PlaygroundMessage firstMessage = new PlaygroundMessage(PlaygroundSignalKey.RUN);
    PlaygroundMessage secondMessage = new PlaygroundMessage(PlaygroundSignalKey.EXIT);
    unitUnderTest.sendMessage(firstMessage);
    unitUnderTest.sendMessage(secondMessage);
    unitUnderTest.sendBatchedMessages();
    verify(outputAdapter, times(1)).sendMessage(any(PlaygroundMessage.class));
  }
}
