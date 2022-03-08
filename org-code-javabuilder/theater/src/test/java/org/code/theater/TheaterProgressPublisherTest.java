package org.code.theater;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageDetailKeys;
import org.code.protocol.OutputAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TheaterProgressPublisherTest {

  private OutputAdapter outputAdapter;
  private ArgumentCaptor<ClientMessage> messageCaptor;
  private TheaterProgressPublisher unitUnderTest;

  @BeforeEach
  public void setUp() {
    outputAdapter = mock(OutputAdapter.class);
    messageCaptor = ArgumentCaptor.forClass(ClientMessage.class);
    unitUnderTest = new TheaterProgressPublisher(outputAdapter);
  }

  @Test
  public void testOnPausePublishesUpdateAfterTimeThreshold() {
    unitUnderTest.onPause(2.0);
    verify(outputAdapter, never()).sendMessage(any());
    unitUnderTest.onPause(2.0);
    verify(outputAdapter, never()).sendMessage(any());

    // Should send message after > 5s
    unitUnderTest.onPause(2.0);
    verify(outputAdapter).sendMessage(messageCaptor.capture());

    final ClientMessage message = messageCaptor.getValue();
    assertEquals("6", message.getDetail().get(ClientMessageDetailKeys.PROGRESS_TIME));
  }

  @Test
  public void testOnPlayPublishesTotalTime() {
    unitUnderTest.onPlay(10.0);
    verify(outputAdapter).sendMessage(messageCaptor.capture());

    final ClientMessage message = messageCaptor.getValue();
    assertEquals("10", message.getDetail().get(ClientMessageDetailKeys.TOTAL_TIME));
  }

  @Test
  public void testOnPlayPublishesTotalPauseTimeIfGreater() {
    unitUnderTest.onPause(15.0);
    unitUnderTest.onPlay(10.0);
    verify(outputAdapter, times(2)).sendMessage(messageCaptor.capture());

    final ClientMessage message = messageCaptor.getAllValues().get(1);
    // Should use total pause time since it's greater
    assertEquals("15", message.getDetail().get(ClientMessageDetailKeys.TOTAL_TIME));
  }
}
