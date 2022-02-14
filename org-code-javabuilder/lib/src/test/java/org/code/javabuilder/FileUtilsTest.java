package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.*;

import org.code.javabuilder.util.FileUtils;
import org.junit.jupiter.api.Test;

class FileUtilsTest {

  @Test
  public void testIsJavaFile() {
    String fileName = "MyClass.java";
    assertTrue(FileUtils.isJavaFile(fileName));

    fileName = ".java"; // At least one character before extension is required
    assertFalse(FileUtils.isJavaFile(fileName));

    fileName = "MyClass.txt"; // Java extension required
    assertFalse(FileUtils.isJavaFile(fileName));

    fileName = "MyClass.Java"; // Java extension must be lowercase
    assertFalse(FileUtils.isJavaFile(fileName));
  }
}
