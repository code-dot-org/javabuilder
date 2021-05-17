package org.code.neighborhood;

import org.code.util.ClientMessage;
import org.code.util.ClientMessageType;

import java.util.HashMap;

public class NeighborhoodSignalMessage extends ClientMessage {
  NeighborhoodSignalMessage(NeighborhoodSignalKey key, HashMap<String, String> detail) {
    super(ClientMessageType.NEIGHBORHOOD, key.toString(), detail);
  }
}
