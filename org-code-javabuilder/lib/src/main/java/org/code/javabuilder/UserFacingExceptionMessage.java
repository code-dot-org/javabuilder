package org.code.javabuilder;

import java.util.HashMap;

public class UserFacingExceptionMessage extends ClientMessage {
  UserFacingExceptionMessage(UserFacingExceptionKey key, HashMap<String, String> detail) {
    super(ClientMessageType.exception, key.toString(), detail);
  }
}
