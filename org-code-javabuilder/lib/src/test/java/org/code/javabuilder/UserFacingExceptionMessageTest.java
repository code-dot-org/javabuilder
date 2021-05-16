package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class UserFacingExceptionMessageTest {
  @Test
  public void getFormattedMessageIncludesDetails() {
    HashMap<String, String> details = new HashMap<>();
    details.put("foo", "bar");
    ClientMessage message =
        new UserFacingExceptionMessage(UserFacingExceptionKey.INTERNAL_EXCEPTION, details);
    assertEquals(
        message.getFormattedMessage(),
        "{\"detail\":{\"foo\":\"bar\"},\"type\":\"EXCEPTION\",\"value\":\"INTERNAL_EXCEPTION\"}");
  }

  @Test
  public void getFormattedSkipsDetailsIfMissing() {
    ClientMessage message =
        new UserFacingExceptionMessage(UserFacingExceptionKey.INTERNAL_EXCEPTION, null);
    assertEquals(
        message.getFormattedMessage(), "{\"type\":\"EXCEPTION\",\"value\":\"INTERNAL_EXCEPTION\"}");
  }
}
