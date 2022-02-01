package org.code.validation;

import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;

// Helper class for testing only to enable simulating multiple ClientMessage types.
public class ClientMessageHelper extends ClientMessage {
  public ClientMessageHelper(ClientMessageType type, String value) {
    super(type, value);
  }
}
