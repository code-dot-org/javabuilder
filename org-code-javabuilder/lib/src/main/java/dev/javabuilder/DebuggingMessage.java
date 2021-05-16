package dev.javabuilder;

import org.code.util.ClientMessage;
import org.code.util.ClientMessageType;

/** A message directed to the client's terminal. Equivalent to System.out.print. */
public class DebuggingMessage extends ClientMessage {
  DebuggingMessage(String value) {
    super(ClientMessageType.DEBUG, value, null);
  }
}
