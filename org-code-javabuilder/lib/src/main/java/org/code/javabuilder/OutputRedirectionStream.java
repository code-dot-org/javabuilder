package org.code.javabuilder;

import java.io.IOException;
import java.io.OutputStream;

public class OutputRedirectionStream extends OutputStream {
  private final OutputAdapter outputAdapter;
  private StringBuilder buffer;
  public OutputRedirectionStream(OutputAdapter outputAdapter) {
    super();
    this.outputAdapter = outputAdapter;
    this.buffer = new StringBuilder();
  }

  @Override
  public void write(int b) throws IOException {
    buffer.append((char)b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    }
    if (off < 0 || len < 0 || off + len > b.length) {
      throw new IndexOutOfBoundsException();
    }
    for (int i = off; i < off + len; i++) {
      buffer.append((char) b[i]);
    }
  }

  @Override
  public void flush() {
    if (buffer.length() == 0) {
      return;
    }
    outputAdapter.sendMessage(buffer.toString());
    buffer.delete(0, buffer.length());
  }
}