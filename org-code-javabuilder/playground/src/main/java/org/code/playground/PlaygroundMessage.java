package org.code.playground;

import java.util.HashMap;
import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;

public class PlaygroundMessage extends ClientMessage {

  PlaygroundMessage(PlaygroundSignalKey key, HashMap<String, String> detail) {
    super(ClientMessageType.PLAYGROUND, key.toString(), detail);
  }
}
