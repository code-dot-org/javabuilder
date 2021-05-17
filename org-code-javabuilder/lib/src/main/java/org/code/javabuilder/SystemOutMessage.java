package org.code.javabuilder;

import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;

/** A message directed to the client's terminal. Equivalent to System.out.print. */
public class SystemOutMessage extends ClientMessage {
  public SystemOutMessage(String value) {
    super(ClientMessageType.SYSTEM_OUT, value, null);
  }
}
