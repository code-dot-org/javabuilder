package org.code.javabuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import org.code.protocol.InputAdapter;

/**
 * An InputStream that queries an InputAdapter for new bytes. This is intended to redirect system.in
 * to use the InputAdapter rather than the server's console. See
 * https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html for full implementation
 * details & contract.
 */
public class InputRedirectionStream extends InputStream {
  private final Queue<Byte> queue;
  private final InputAdapter inputAdapter;

  public InputRedirectionStream(InputAdapter inputAdapter) {
    this.inputAdapter = inputAdapter;
    this.queue = new LinkedList<>();
  }

  /**
   * See: https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#read-- Checks the queue
   * for existing bytes. If the queue is empty, polls the inputAdapter for new data. This is a
   * blocking call.
   *
   * @return the first byte in the queue
   */
  @Override
  public int read() {
    if (queue.peek() == null) {
      byte[] message = inputAdapter.getNextMessage().getBytes(StandardCharsets.UTF_8);
      for (byte b : message) {
        queue.add(b);
      }
    }

    return (int) queue.remove();
  }

  /** See: https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#read-byte:A- */
  @Override
  public int read(byte[] b) {
    return this.read(b, 0, b.length);
  }

  /**
   * See: https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#read-byte:A-int-int-
   */
  @Override
  public int read(byte[] b, int off, int len) {
    if (b == null) {
      throw new NullPointerException();
    }
    if (off < 0 || len < 0 || len > b.length - off) {
      throw new IndexOutOfBoundsException();
    }

    int k = 0;
    while (k < len) {
      b[off + k] = (byte) this.read();
      k++;
      if (queue.peek() == null) {
        break;
      }
    }

    return k;
  }

  /**
   * See: https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#skip-long-
   *
   * @throws IOException Per the oracle docs, this can throw an exception if we choose not to
   *     implement skip. Skip does not make sense for our uses, so we throw the exception.
   */
  @Override
  public long skip(long n) throws IOException {
    throw new IOException();
  }

  /** See: https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#available-- */
  @Override
  public int available() {
    return queue.size();
  }

  /**
   * See: https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#markSupported--
   *
   * @return false always. We do not need to support the `reset` or `mark` methods at this time.
   */
  @Override
  public boolean markSupported() {
    return false;
  }
}
