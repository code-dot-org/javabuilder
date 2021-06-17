package org.code.protocol;

public interface InputAdapter {
  /** @return The next user input to the currently running program */
  String getNextMessage();
}
