package org.code.javabuilder;

import java.util.HashMap;

public class UserInitiatedExceptionMessage extends ClientMessage {
  UserInitiatedExceptionMessage(UserInitiatedExceptionKey key, HashMap<String, String> detail) {
    super(ClientMessageType.exception, key.toString(), detail);
  }
}
