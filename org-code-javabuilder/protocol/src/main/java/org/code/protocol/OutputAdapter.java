package org.code.protocol;

import java.nio.ByteBuffer;

public interface OutputAdapter {
  /** @param message An output from the user program */
  void sendMessage(ClientMessage message);

  void sendBinaryMessage(ByteBuffer bytes);
}
