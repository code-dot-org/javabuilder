package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FileNameUtilsTest {

  @Test
  public void testIsJavaFile() {
    String fileName = "MyClass.java";
    assertTrue(FileNameUtils.isJavaFile(fileName));

    fileName = ".java"; // At least one character before extension is required
    assertFalse(FileNameUtils.isJavaFile(fileName));

    fileName = "MyClass.txt"; // Java extension required
    assertFalse(FileNameUtils.isJavaFile(fileName));

    fileName = "MyClass.Java"; // Java extension must be lowercase
    assertFalse(FileNameUtils.isJavaFile(fileName));
  }
}
