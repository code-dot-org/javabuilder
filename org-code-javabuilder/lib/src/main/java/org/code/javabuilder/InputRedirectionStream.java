package org.code.javabuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;

public class InputRedirectionStream extends InputStream {
  private final Queue<Byte> queue;
  private final InputAdapter inputAdapter;

  public InputRedirectionStream(InputAdapter inputAdapter) {
    this.inputAdapter = inputAdapter;
    this.queue = new LinkedList<>();
  }

  @Override
  public int read() {
    if (queue.peek() == null) {
      byte[] message = inputAdapter.getNextMessage().getBytes(StandardCharsets.UTF_8);
      for (byte b : message) {
        queue.add(b);
      }
    }

    return (int)queue.remove();
  }

  @Override
  public int read(byte[] b) {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) {
    if(b == null) {
      throw new NullPointerException();
    }
    if(off < 0 || len < 0 || len > b.length - off) {
      throw new IndexOutOfBoundsException();
    }

    int k = 0;
    while (k < len) {
      b[k + off] = (byte)read();
      k++;
      if (queue.peek() == null) {
        break;
      }
    }

    return k;
  }

  @Override
  public long skip(long n) throws IOException {
    throw new IOException();
  }

  @Override
  public int available() {
    return queue.size();
  }

  @Override
  public boolean markSupported() {
    return false;
  }
}
