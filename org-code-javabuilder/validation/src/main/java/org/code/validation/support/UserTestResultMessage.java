package org.code.validation.support;

import java.util.HashMap;
import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;

/** A message describing a test result directed to the client's terminal. */
public class UserTestResultMessage extends ClientMessage {
  public UserTestResultMessage(UserTestResultSignalKey key, HashMap<String, String> details) {
    super(ClientMessageType.TEST_RESULT, key.toString(), details);
  }
}
