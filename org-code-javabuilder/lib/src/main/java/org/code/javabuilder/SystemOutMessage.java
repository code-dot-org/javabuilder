package org.code.javabuilder;

/** A message directed to the client's terminal. Equivalent to System.out.print. */
public class SystemOutMessage extends ClientMessage {
  public SystemOutMessage(String value) {
    super(ClientMessageType.systemOut, value, null);
  }
}
