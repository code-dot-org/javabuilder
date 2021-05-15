package org.code.javabuilder;

import java.util.HashMap;

/** An error message directed to the user, caused by the user. */
public class UserInitiatedExceptionMessage extends ClientMessage {
  UserInitiatedExceptionMessage(UserInitiatedExceptionKey key, HashMap<String, String> detail) {
    super(ClientMessageType.EXCEPTION, key.toString(), detail);
  }
}
