package org.code.playground;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
  public void testSendMessageCallsOutputAdapter() {
    final PlaygroundMessage message = new PlaygroundMessage(PlaygroundSignalKey.RUN);

    unitUnderTest.sendMessage(message);

    verify(outputAdapter).sendMessage(message);
  }
}
