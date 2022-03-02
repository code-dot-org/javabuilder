package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

  @Test
  public void automaticallyImportCustomSystemOnCreate() throws UserInitiatedException {
    String originalCode = "public class MyClass {}";
    JavaProjectFile unitUnderTest = new JavaProjectFile("MyClass.java", originalCode);
    String expectedCode = "import org.code.lang.System;\n" + originalCode;
    assertEquals(expectedCode, unitUnderTest.getFileContents());
  }

  @Test
  public void automaticallyImportCustomSystemOnSet() throws UserInitiatedException {
    String originalCode = "public class MyClass {}";
    JavaProjectFile unitUnderTest = new JavaProjectFile("MyClass.java");
    unitUnderTest.setFileContents(originalCode);
    String expectedCode = "import org.code.lang.System;\n" + originalCode;
    assertEquals(expectedCode, unitUnderTest.getFileContents());
  }
}
