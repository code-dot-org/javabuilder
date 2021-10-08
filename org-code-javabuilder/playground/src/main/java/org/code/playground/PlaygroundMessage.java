package org.code.playground;

import java.util.HashMap;
import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;
import org.json.JSONObject;

public class PlaygroundMessage extends ClientMessage {
  PlaygroundMessage(PlaygroundSignalKey key, HashMap<String, String> detail) {
    super(ClientMessageType.PLAYGROUND, key.toString(), detail);
  }

  PlaygroundMessage(PlaygroundSignalKey key) {
    super(ClientMessageType.PLAYGROUND, key.toString());
  }

  PlaygroundMessage(PlaygroundSignalKey key, JSONObject detail) {
    super(ClientMessageType.PLAYGROUND, key.toString(), detail);
  }
}
