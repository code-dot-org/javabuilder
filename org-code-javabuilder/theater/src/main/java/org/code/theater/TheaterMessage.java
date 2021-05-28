package org.code.theater;

import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;

import java.util.HashMap;

public class TheaterMessage extends ClientMessage {
  TheaterMessage(TheaterSignalKey key, HashMap<String, String> detail) {
    super(ClientMessageType.THEATER, key.toString(), detail);
  }
}
