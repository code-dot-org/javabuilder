package org.code.protocol;

public interface InputAdapter {
  /** @return The next user input to the currently running program */
  String getNextMessage();

  /**
   * @return If there is still an active connection to the input. If false is returned, it will
   *     never return true again.
   */
  boolean hasActiveConnection();
}
