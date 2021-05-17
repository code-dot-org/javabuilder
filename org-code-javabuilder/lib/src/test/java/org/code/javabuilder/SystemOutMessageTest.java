package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.code.protocol.ClientMessage;
import org.junit.jupiter.api.Test;

public class SystemOutMessageTest {
  @Test
  public void getFormattedMessageUsesNoDetails() {
    ClientMessage message = new SystemOutMessage("Hello world");
    assertEquals(
        message.getFormattedMessage(), "{\"type\":\"SYSTEM_OUT\",\"value\":\"Hello world\"}");
  }
}
