package org.code.protocol;

public interface OutputAdapter {
  /** @param message An output from the user program */
  void sendMessage(ClientMessage message);

  boolean hasActiveConnection();
}
