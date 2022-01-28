package org.code.validation;

import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;

/** A message describing a test result directed to the client's terminal. */
public class TestResultMessage extends ClientMessage {
  public TestResultMessage(String value) {
    super(ClientMessageType.TEST_RESULT, value);
  }
}
