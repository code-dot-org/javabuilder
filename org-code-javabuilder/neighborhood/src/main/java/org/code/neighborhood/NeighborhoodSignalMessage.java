package org.code.neighborhood;

import java.util.HashMap;
import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;

public class NeighborhoodSignalMessage extends ClientMessage {
  public NeighborhoodSignalMessage(NeighborhoodSignalKey key, HashMap<String, String> detail) {
    super(ClientMessageType.NEIGHBORHOOD, key.toString(), detail);
  }
}
