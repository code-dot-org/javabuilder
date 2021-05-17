package org.code.neighborhood;

import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;

import java.util.HashMap;

public class NeighborhoodSignalMessage extends ClientMessage {
  NeighborhoodSignalMessage(NeighborhoodSignalKey key, HashMap<String, String> detail) {
    super(ClientMessageType.NEIGHBORHOOD, key.toString(), detail);
  }
}
