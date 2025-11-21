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

  @Test
  public void constructorThrowsErrorForJavaLangPackage() {
    // Test the vulnerability: users should not be able to create java.lang.Runtime.java
    UserInitiatedException exception =
        assertThrows(
            UserInitiatedException.class,
            () -> {
              new JavaProjectFile("java.lang.Runtime.java");
            });
    assertEquals(
        UserInitiatedExceptionKey.INVALID_JAVA_FILE_NAME.toString(), exception.getMessage());
  }

  @Test
  public void constructorThrowsErrorForJavaLangProcess() {
    UserInitiatedException exception =
        assertThrows(
            UserInitiatedException.class,
            () -> {
              new JavaProjectFile("java.lang.Process.java");
            });
    assertEquals(
        UserInitiatedExceptionKey.INVALID_JAVA_FILE_NAME.toString(), exception.getMessage());
  }

  @Test
  public void constructorThrowsErrorForJavaNetSocket() {
    UserInitiatedException exception =
        assertThrows(
            UserInitiatedException.class,
            () -> {
              new JavaProjectFile("java.net.Socket.java");
            });
    assertEquals(
        UserInitiatedExceptionKey.INVALID_JAVA_FILE_NAME.toString(), exception.getMessage());
  }

  @Test
  public void constructorThrowsErrorForJavaNioFiles() {
    UserInitiatedException exception =
        assertThrows(
            UserInitiatedException.class,
            () -> {
              new JavaProjectFile("java.nio.file.Files.java");
            });
    assertEquals(
        UserInitiatedExceptionKey.INVALID_JAVA_FILE_NAME.toString(), exception.getMessage());
  }

  @Test
  public void constructorThrowsErrorForJavaxScriptEngine() {
    UserInitiatedException exception =
        assertThrows(
            UserInitiatedException.class,
            () -> {
              new JavaProjectFile("javax.script.ScriptEngine.java");
            });
    assertEquals(
        UserInitiatedExceptionKey.INVALID_JAVA_FILE_NAME.toString(), exception.getMessage());
  }

  @Test
  public void constructorThrowsErrorForSunMiscUnsafe() {
    UserInitiatedException exception =
        assertThrows(
            UserInitiatedException.class,
            () -> {
              new JavaProjectFile("sun.misc.Unsafe.java");
            });
    assertEquals(
        UserInitiatedExceptionKey.INVALID_JAVA_FILE_NAME.toString(), exception.getMessage());
  }

  @Test
  public void constructorAllowsValidUserClasses() throws UserInitiatedException {
    // These should all be allowed
    new JavaProjectFile("MyClass.java");
    new JavaProjectFile("MyClass1.java");
    new JavaProjectFile("MyClass_1.java");
    new JavaProjectFile("My-Class.java");
  }

  @Test
  public void constructorThrowsErrorForInvalidUserClasses() {
    UserInitiatedException exception =
        assertThrows(
            UserInitiatedException.class,
            () -> {
              new JavaProjectFile("My.Class.java");
            });
    assertEquals(
        UserInitiatedExceptionKey.INVALID_JAVA_FILE_NAME.toString(), exception.getMessage());
  }
}
