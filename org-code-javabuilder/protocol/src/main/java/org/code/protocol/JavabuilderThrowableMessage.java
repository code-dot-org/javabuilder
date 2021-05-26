package org.code.protocol;

import java.util.HashMap;

/** An error message directed to the user. */
public class JavabuilderThrowableMessage extends ClientMessage {
  public JavabuilderThrowableMessage(Enum key, HashMap<String, String> detail) {
    super(ClientMessageType.EXCEPTION, key.toString(), detail);
  }
}
