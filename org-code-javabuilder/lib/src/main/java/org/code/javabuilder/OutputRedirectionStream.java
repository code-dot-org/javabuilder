package org.code.javabuilder;

import java.io.OutputStream;
import org.code.protocol.OutputAdapter;

/**
 * An OutputStream that passes output to an OutputAdapter. It is intended to redirect output from
 * the user program that is intended for the console. See
 * https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html for full implementation
 * details and contract.
 */
public class OutputRedirectionStream extends OutputStream {
  private final OutputAdapter outputAdapter;
  private final StringBuilder buffer;

  public OutputRedirectionStream(OutputAdapter outputAdapter) {
    super();
    this.outputAdapter = outputAdapter;
    this.buffer = new StringBuilder();
  }

  /**
   * See: https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html#write-int- Adds the
   * given byte to the output buffer.
   *
   * @param b The byte to add to the buffer
   */
  @Override
  public void write(int b) {
    buffer.append((char) b);
  }

  /** See: https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html#write-byte:A- */
  @Override
  public void write(byte[] b) {
    this.write(b, 0, b.length);
  }

  /**
   * See: https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html#write-byte:A-int-int-
   */
  @Override
  public void write(byte[] b, int off, int len) {
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

  /**
   * See: https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html#flush-- Writes the
   * buffer to the OutputAdapter and clears the buffer.
   */
  @Override
  public void flush() {
    if (buffer.length() == 0) {
      return;
    }

    outputAdapter.sendMessage(new SystemOutMessage(buffer.toString()));
    buffer.delete(0, buffer.length());
  }
}
