package org.code.javabuilder;

public interface OutputAdapter {
  /** @param message An output from the user program */
  void sendMessage(ClientMessage message);
}
