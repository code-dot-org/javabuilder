package org.code.protocol;

public interface OutputAdapter {
  /** @param message An output from the user program */
  void sendMessage(ClientMessage message);

  /**
   * @return If there is still an active connection to the output. If false is returned, it will
   *     never return true again.
   */
  boolean hasActiveConnection();
}
