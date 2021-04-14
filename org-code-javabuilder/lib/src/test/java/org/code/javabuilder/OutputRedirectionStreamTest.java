package org.code.javabuilder;

import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    verify(outputAdapter, times(0)).sendMessage(anyString());
  }

  @Test
  public void writeAddsASingleCharToBuffer() {
    stream.write('a');
    stream.flush();
    verify(outputAdapter, times(1)).sendMessage("a");
  }

  @Test
  public void flushRemovesAllCharsFromBuffer() {
    stream.write('a');
    stream.write('b');
    stream.write('c');
    stream.flush();
    stream.flush();
    verify(outputAdapter, times(1)).sendMessage("abc");
  }

  @Test
  public void arrayWriteAddsSpecifiedChars() {
    byte[] arr = "hello world".getBytes(StandardCharsets.UTF_8);
    stream.write(arr, 1, 4);
    stream.flush();
    verify(outputAdapter, times(1)).sendMessage("ello");
  }
}
