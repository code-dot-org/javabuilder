package dev.javabuilder;

import org.code.javabuilder.ClientMessage;
import org.code.javabuilder.ClientMessageType;

/** A message directed to the client's terminal. Equivalent to System.out.print. */
public class DebuggingMessage extends ClientMessage {
  DebuggingMessage(String value) {
    super(ClientMessageType.debug, value, null);
  }
}
