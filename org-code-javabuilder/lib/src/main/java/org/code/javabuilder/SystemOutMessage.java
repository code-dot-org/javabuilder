package org.code.javabuilder;

import org.code.util.ClientMessage;
import org.code.util.ClientMessageType;

/** A message directed to the client's terminal. Equivalent to System.out.print. */
public class SystemOutMessage extends ClientMessage {
  public SystemOutMessage(String value) {
    super(ClientMessageType.SYSTEM_OUT, value, null);
  }
}
