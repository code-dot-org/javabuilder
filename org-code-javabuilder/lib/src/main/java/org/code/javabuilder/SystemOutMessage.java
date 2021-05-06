package org.code.javabuilder;

public class SystemOutMessage extends ClientMessage {
  public SystemOutMessage(String value) {
    super(ClientMessageType.systemOut, value, null);
  }
}
