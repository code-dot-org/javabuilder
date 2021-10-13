package org.code.playground;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import org.code.protocol.ClientMessageDetailKeys;
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

  @Test
  public void testSendBatchedMessagesSplitsLargeBatch() {
    HashMap<String, String> largeDetails = new HashMap<>();
    char[] largeCharArray = new char[50000];
    Arrays.fill(largeCharArray, 'a');
    largeDetails.put(ClientMessageDetailKeys.TEXT, new String(largeCharArray));
    PlaygroundMessage sampleLargeMessage =
        new PlaygroundMessage(PlaygroundSignalKey.ADD_TEXT_ITEM, largeDetails);
    unitUnderTest.sendMessage(sampleLargeMessage);
    unitUnderTest.sendMessage(sampleLargeMessage);
    unitUnderTest.sendMessage(sampleLargeMessage);
    unitUnderTest.sendBatchedMessages();
    // 3 50,000 character messages will should be split into 2 messages, since our limit is 120,000
    // characters
    verify(outputAdapter, times(2)).sendMessage(any(PlaygroundMessage.class));
  }
}
