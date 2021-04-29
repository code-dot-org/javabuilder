package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class JavaProjectFileTest {
  @Test
  public void constructorThrowsErrorIfFileDoesNotEndInJava() {
    assertThrows(
        UserInitiatedException.class,
        () -> {
          new JavaProjectFile("stringWithBadExtension.jar");
        });
  }
}
