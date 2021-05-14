package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UserFacingExceptionTest {
  @Test
  public void getExceptionMessageIncludesConnectionId() {
    UserFacingException exception =
        new UserFacingException(UserFacingExceptionKey.INTERNAL_EXCEPTION);
    UserFacingExceptionMessage message = exception.getExceptionMessage();
    assertEquals(message.getDetail().get("connectionId"), Properties.getConnectionId());
  }

  @Test
  public void getExceptionMessageIncludesCause() {
    UserFacingException exception =
        new UserFacingException(
            UserFacingExceptionKey.INTERNAL_EXCEPTION, new Exception("the cause of the exception"));
    UserFacingExceptionMessage message = exception.getExceptionMessage();
    assertTrue(message.getDetail().get("cause").contains("the cause of the exception"));
  }
}
