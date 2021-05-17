package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UserInitiatedExceptionTest {
  @Test
  public void getExceptionMessageIncludesCause() {
    UserInitiatedException exception =
        new UserInitiatedException(
            UserInitiatedExceptionKey.COMPILER_ERROR, new Exception("the cause of the exception"));
    UserInitiatedExceptionMessage message = exception.getExceptionMessage();
    assertTrue(message.getDetail().getString("cause").contains("the cause of the exception"));
  }
}
