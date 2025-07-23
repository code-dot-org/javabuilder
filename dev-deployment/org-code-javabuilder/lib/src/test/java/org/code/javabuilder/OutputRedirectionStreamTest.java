package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import org.code.protocol.OutputAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class OutputRedirectionStreamTest {
  OutputAdapter outputAdapter;
  OutputRedirectionStream stream;

  @BeforeEach
  public void setUp() {
    outputAdapter = mock(OutputAdapter.class);
    stream = new OutputRedirectionStream(outputAdapter);
  }

  @Test
  public void flushDoesNothingIfNoBytesArePresent() {
    stream.flush();
    verify(outputAdapter, times(0)).sendMessage(any(SystemOutMessage.class));
  }

  @Test
  public void writeAddsASingleCharToBuffer() {
    stream.write('a');
    stream.flush();
    ArgumentCaptor<SystemOutMessage> message = ArgumentCaptor.forClass(SystemOutMessage.class);
    verify(outputAdapter, times(1)).sendMessage(message.capture());
    assertEquals(message.getValue().getValue(), "a");
  }

  @Test
  public void flushRemovesAllCharsFromBuffer() {
    stream.write('a');
    stream.write('b');
    stream.write('c');
    stream.flush();
    stream.flush();
    ArgumentCaptor<SystemOutMessage> message = ArgumentCaptor.forClass(SystemOutMessage.class);
    verify(outputAdapter, times(1)).sendMessage(message.capture());
    assertEquals(message.getValue().getValue(), "abc");
  }

  @Test
  public void arrayWriteAddsSpecifiedChars() {
    byte[] arr = "hello world".getBytes(StandardCharsets.UTF_8);
    stream.write(arr, 1, 4);
    stream.flush();
    ArgumentCaptor<SystemOutMessage> message = ArgumentCaptor.forClass(SystemOutMessage.class);
    verify(outputAdapter, times(1)).sendMessage(message.capture());
    assertEquals(message.getValue().getValue(), "ello");
  }
}
