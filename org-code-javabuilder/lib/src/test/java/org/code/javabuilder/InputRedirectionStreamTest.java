package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InputRedirectionStreamTest {
  private InputRedirectionStream stream;
  private InputAdapter inputAdapter;

  @BeforeEach
  public void setUp() {
    inputAdapter = mock(InputAdapter.class);
    when(inputAdapter.getNextMessage()).thenReturn("hello world");
    stream = new InputRedirectionStream(inputAdapter);
  }

  @Test
  public void readsTheFirstByteFromTheInputAdapter() {
    assertEquals(stream.read(), 'h');
  }

  @Test
  public void cachesBytesFromInputAdapter() {
    stream.read();
    assertEquals(stream.read(), 'e');
    verify(inputAdapter, times(1)).getNextMessage();
  }

  @Test
  public void availableReturnsRemainingSizeOfStream() {
    stream.read();
    assertEquals(stream.available(), 10);
  }

  @Test
  public void arrayReadFillsSpecifiedSpace() {
    byte[] b = new byte[7];
    stream.read(b, 1, 5);

    assertArrayEquals(new byte[] {0, 'h', 'e', 'l', 'l', 'o', 0}, b);
  }
}
