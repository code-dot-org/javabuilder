package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.code.protocol.InternalExceptionKey;
import org.code.protocol.JavabuilderThrowableMessage;
import org.code.protocol.Properties;
import org.junit.jupiter.api.Test;

public class InternalServerExceptionTest {
  @Test
  public void getExceptionMessageIncludesConnectionId() {
    InternalServerException exception =
        new InternalServerException(InternalExceptionKey.INTERNAL_EXCEPTION);
    JavabuilderThrowableMessage message = exception.getExceptionMessage();
    assertEquals(message.getDetail().get("connectionId"), Properties.getConnectionId());
  }

  @Test
  public void getExceptionMessageIncludesCause() {
    InternalServerException exception =
        new InternalServerException(
            InternalExceptionKey.INTERNAL_EXCEPTION, new Exception("the cause of the exception"));
    JavabuilderThrowableMessage message = exception.getExceptionMessage();
    assertTrue(message.getDetail().getString("cause").contains("the cause of the exception"));
  }
}
