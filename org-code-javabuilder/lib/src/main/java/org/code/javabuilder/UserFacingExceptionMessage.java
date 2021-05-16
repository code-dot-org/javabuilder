package org.code.javabuilder;

import java.util.HashMap;
import org.code.util.ClientMessage;
import org.code.util.ClientMessageType;

/** An error message directed to the user. Equivalent of a user-visible 500 error. */
public class UserFacingExceptionMessage extends ClientMessage {
  UserFacingExceptionMessage(UserFacingExceptionKey key, HashMap<String, String> detail) {
    super(ClientMessageType.EXCEPTION, key.toString(), detail);
  }
}
