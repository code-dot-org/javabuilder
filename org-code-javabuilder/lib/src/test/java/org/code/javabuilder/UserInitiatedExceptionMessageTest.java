package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import org.code.protocol.ClientMessage;
import org.junit.jupiter.api.Test;

public class UserInitiatedExceptionMessageTest {
  @Test
  public void getFormattedMessageIncludesDetails() {
    HashMap<String, String> details = new HashMap<>();
    details.put("foo", "bar");
    ClientMessage message =
        new UserInitiatedExceptionMessage(UserInitiatedExceptionKey.COMPILER_ERROR, details);
    assertEquals(
        message.getFormattedMessage(),
        "{\"detail\":{\"foo\":\"bar\"},\"type\":\"EXCEPTION\",\"value\":\"COMPILER_ERROR\"}");
  }

  @Test
  public void getFormattedSkipsDetailsIfMissing() {
    ClientMessage message =
        new UserInitiatedExceptionMessage(UserInitiatedExceptionKey.COMPILER_ERROR, null);
    assertEquals(
        message.getFormattedMessage(), "{\"type\":\"EXCEPTION\",\"value\":\"COMPILER_ERROR\"}");
  }
}
