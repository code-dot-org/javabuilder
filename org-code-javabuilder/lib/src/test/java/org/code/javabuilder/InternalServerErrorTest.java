package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.code.protocol.InternalErrorKey;
import org.code.protocol.JavabuilderThrowableMessage;
import org.code.protocol.Properties;
import org.junit.jupiter.api.Test;

public class InternalServerErrorTest {
  @Test
  public void getExceptionMessageIncludesConnectionId() {
    InternalServerError exception = new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION);
    JavabuilderThrowableMessage message = exception.getExceptionMessage();
    assertEquals(message.getDetail().get("connectionId"), Properties.getConnectionId());
  }

  @Test
  public void getExceptionMessageIncludesCause() {
    InternalServerError exception =
        new InternalServerError(
            InternalErrorKey.INTERNAL_EXCEPTION, new Exception("the cause of the exception"));
    JavabuilderThrowableMessage message = exception.getExceptionMessage();
    assertTrue(message.getDetail().getString("cause").contains("the cause of the exception"));
  }
}
