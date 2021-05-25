package org.code.protocol;

import org.code.protocol.ClientMessage;

public interface OutputAdapter {
  /** @param message An output from the user program */
  void sendMessage(ClientMessage message);
}
