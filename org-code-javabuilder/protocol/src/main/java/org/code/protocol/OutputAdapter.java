package org.code.protocol;

import java.io.InputStream;

public interface OutputAdapter {
  /** @param message An output from the user program */
  void sendMessage(ClientMessage message);

  String writeToFile(String filename, InputStream input) throws JavabuilderException;
}
