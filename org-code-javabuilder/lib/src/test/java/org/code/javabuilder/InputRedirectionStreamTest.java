package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import org.code.protocol.InputAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InputRedirectionStreamTest {
  private InputRedirectionStream stream;
  private InputAdapter inputAdapter;

  @BeforeEach
  public void setUp() {
    inputAdapter = mock(InputAdapter.class);
    stream = new InputRedirectionStream(inputAdapter);
  }

  @Test
  public void readsTheFirstByteFromTheInputAdapter() {
    when(inputAdapter.getNextMessage()).thenReturn("hi");
    assertEquals(stream.read(), 'h');
  }

  @Test
  public void cachesBytesFromInputAdapter() {
    when(inputAdapter.getNextMessage()).thenReturn("hi");
    stream.read();
    assertEquals(stream.read(), 'i');
    verify(inputAdapter, times(1)).getNextMessage();
  }

  @Test
  public void availableReturnsRemainingSizeOfStream() {
    when(inputAdapter.getNextMessage()).thenReturn("hello world");
    stream.read();
    assertEquals(stream.available(), 10);
  }

  @Test
  public void arrayReadFillsSpecifiedSpace() {
    when(inputAdapter.getNextMessage()).thenReturn("hello world");
    byte[] b = new byte[7];
    stream.read(b, 1, 5);

    assertArrayEquals(new byte[] {0, 'h', 'e', 'l', 'l', 'o', 0}, b);
  }

  @Test
  public void unboundedArrayReadFillsArray() {
    when(inputAdapter.getNextMessage()).thenReturn("hello world");
    byte[] b = new byte[5];
    stream.read(b);

    assertArrayEquals(new byte[] {'h', 'e', 'l', 'l', 'o'}, b);
  }

  @Test
  public void arrayReadStopsWhenInputEnds() {
    when(inputAdapter.getNextMessage()).thenReturn("hi");
    byte[] b = new byte[3];
    stream.read(b);

    assertArrayEquals(new byte[] {'h', 'i', 0}, b);
  }

  @Test
  public void arrayThrowsIfArrayIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> {
          stream.read(null, 0, 0);
        });
  }

  @Test
  public void arrayThrowsWithBadInputs() {
    byte[] b = new byte[3];
    assertThrows(
        IndexOutOfBoundsException.class,
        () -> {
          stream.read(b, -1, 0);
        });
    assertThrows(
        IndexOutOfBoundsException.class,
        () -> {
          stream.read(b, 0, -1);
        });
    assertThrows(
        IndexOutOfBoundsException.class,
        () -> {
          stream.read(b, 0, 10);
        });
  }

  @Test
  public void markSupportedReturnsFalse() {
    assertFalse(stream.markSupported());
  }

  @Test
  public void skipThrowsIOException() {
    assertThrows(
        IOException.class,
        () -> {
          stream.skip(2);
        });
  }
}
