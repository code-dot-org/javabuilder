package org.code.javabuilder;

import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;

/** A message directed to the client's terminal. Equivalent to System.out.print. */
public class SystemOutMessage extends ClientMessage {
  private static int MESSAGE_CHAR_LIMIT = 100;

  public SystemOutMessage(String value) {
    super(ClientMessageType.SYSTEM_OUT, trimMessage(value));
  }

  private static String trimMessage(String value) {
    if (value != null && value.length() > MESSAGE_CHAR_LIMIT) {
      value = value.substring(0, MESSAGE_CHAR_LIMIT);
    }
    return value;
  }
}
