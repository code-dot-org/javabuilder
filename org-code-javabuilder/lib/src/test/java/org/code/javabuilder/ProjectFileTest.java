package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ProjectFileTest {
  @Test
  public void constructorThrowsErrorIfFileDoesNotEndInJava() {
    assertThrows(
        UserInitiatedException.class,
        () -> {
          new ProjectFile("stringWithBadExtension.jar");
        });
  }
}
