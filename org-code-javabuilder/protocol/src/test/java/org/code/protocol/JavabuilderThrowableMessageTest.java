package org.code.protocol;

import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JavabuilderThrowableMessageTest {
  enum ExceptionKey {
    SAMPLE_EXCEPTION
  }

  @Test
  public void getFormattedMessageIncludesDetails() {
    HashMap<String, String> details = new HashMap<>();
    details.put("foo", "bar");
    ClientMessage message = new JavabuilderThrowableMessage(ExceptionKey.SAMPLE_EXCEPTION, details);
    Assertions.assertEquals(
        message.getFormattedMessage(),
        "{\"detail\":{\"foo\":\"bar\"},\"type\":\"EXCEPTION\",\"value\":\"SAMPLE_EXCEPTION\"}");
  }

  @Test
  public void getFormattedSkipsDetailsIfMissing() {
    ClientMessage message = new JavabuilderThrowableMessage(ExceptionKey.SAMPLE_EXCEPTION, null);
    Assertions.assertEquals(
        message.getFormattedMessage(), "{\"type\":\"EXCEPTION\",\"value\":\"SAMPLE_EXCEPTION\"}");
  }
}
