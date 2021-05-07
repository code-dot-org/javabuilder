package org.code.javabuilder;

import java.util.HashMap;

/** An error message directed to the user. Equivalent of a user-visible 500 error. */
public class UserFacingExceptionMessage extends ClientMessage {
  UserFacingExceptionMessage(UserFacingExceptionKey key, HashMap<String, String> detail) {
    super(ClientMessageType.exception, key.toString(), detail);
  }
}
