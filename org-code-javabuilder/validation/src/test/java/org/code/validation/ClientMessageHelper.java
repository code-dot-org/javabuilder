package org.code.validation;

import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;

public class ClientMessageHelper extends ClientMessage {
  public ClientMessageHelper(ClientMessageType type, String value) {
    super(type, value);
  }
}
