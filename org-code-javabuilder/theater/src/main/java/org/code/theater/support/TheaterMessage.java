package org.code.theater.support;

import java.util.HashMap;
import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;

public class TheaterMessage extends ClientMessage {
  public TheaterMessage(TheaterSignalKey key, HashMap<String, String> detail) {
    super(ClientMessageType.THEATER, key.toString(), detail);
  }
}
