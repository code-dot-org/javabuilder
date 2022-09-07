package org.code.neighborhood.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;

public class NeighborhoodSignalMessage extends ClientMessage {
  public NeighborhoodSignalMessage(NeighborhoodSignalKey key, HashMap<String, String> detail) {
    super(ClientMessageType.NEIGHBORHOOD, key.toString(), detail);
  }

  @Override
  public boolean shouldAlwaysSend() {
    String signalKey = this.getValue();
    Set<String> ignoredSignalKeys = new HashSet<>();
    // These keys are only used for validation testing, by default don't send them.
    ignoredSignalKeys.add(NeighborhoodSignalKey.CAN_MOVE.toString());
    ignoredSignalKeys.add(NeighborhoodSignalKey.IS_ON_BUCKET.toString());
    ignoredSignalKeys.add(NeighborhoodSignalKey.IS_ON_PAINT.toString());
    if (ignoredSignalKeys.contains(signalKey)) {
      return false;
    }
    return true;
  }
}
