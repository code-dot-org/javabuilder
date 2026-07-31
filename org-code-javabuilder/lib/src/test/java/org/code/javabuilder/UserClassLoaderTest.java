package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.tools.ToolProvider;
import org.code.javabuilder.util.JarUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the student/validation class loader pair used for validation runs: validation code may use
 * reflection and EasyMock, while student code stays confined to the USER-level allowlist even
 * though validation code loads and invokes it.
 */
class UserClassLoaderTest {
  @TempDir static Path tempDir;
  private static URL[] classLoaderUrls;

  private static final String STUDENT_MAIN_SOURCE =
      "public class StudentMain {\n"
          + "  public int foo() {\n"
          + "    return 1;\n"
          + "  }\n"
          + "\n"
          + "  public static void main(String[] args) {}\n"
          + "}\n";

  // Validation code exercising the validator-only allowances: reflection on a student class.
  private static final String VALIDATION_SOURCE =
      "import java.lang.reflect.Method;\n"
          + "public class Validation {\n"
          + "  public static int callFoo() throws Exception {\n"
          + "    StudentMain student = new StudentMain();\n"
          + "    Method foo = StudentMain.class.getMethod(\"foo\");\n"
          + "    return (Integer) foo.invoke(student);\n"
          + "  }\n"
          + "}\n";

  // Validation code that mocks a student class. EasyMock (cglib) defines the mock subclass inside
  // the student loader, so this only links if the student half of the pair allows the mocking
  // support classes.
  private static final String MOCKING_VALIDATION_SOURCE =
      "import org.easymock.EasyMock;\n"
          + "public class MockingValidation {\n"
          + "  public static int mockFoo() {\n"
          + "    StudentMain mock =\n"
          + "        EasyMock.partialMockBuilder(StudentMain.class)\n"
          + "            .addMockedMethod(\"foo\")\n"
          + "            .createMock();\n"
          + "    EasyMock.expect(mock.foo()).andReturn(42);\n"
          + "    EasyMock.replay(mock);\n"
          + "    return mock.foo();\n"
          + "  }\n"
          + "}\n";

  private static final List<String> STUDENT_CLASSES = List.of("StudentMain");
  private static final List<String> VALIDATION_CLASSES = List.of("Validation", "MockingValidation");

  @BeforeAll
  public static void compileFixtures() throws Exception {
    final Path studentMain = writeSource("StudentMain.java", STUDENT_MAIN_SOURCE);
    final Path validation = writeSource("Validation.java", VALIDATION_SOURCE);
    final Path mockingValidation = writeSource("MockingValidation.java", MOCKING_VALIDATION_SOURCE);
    final int result =
        ToolProvider.getSystemJavaCompiler()
            .run(
                null,
                null,
                null,
                "-d",
                tempDir.toString(),
                "-classpath",
                JarUtils.getAllJarPaths(),
                studentMain.toString(),
                validation.toString(),
                mockingValidation.toString());
    assertEquals(0, result, "fixture compilation failed");
    classLoaderUrls = JarUtils.getAllJarURLs(tempDir.toUri().toURL());
  }

  private static Path writeSource(String fileName, String source) throws Exception {
    final Path file = tempDir.resolve(fileName);
    Files.writeString(file, source);
    return file;
  }

  private static UserClassLoader.ValidatorClassLoaderPair createPair() {
    return UserClassLoader.createValidatorPair(
        classLoaderUrls,
        UserClassLoaderTest.class.getClassLoader(),
        STUDENT_CLASSES,
        VALIDATION_CLASSES);
  }

  @Test
  public void studentClassesAreDefinedOnceByTheStudentLoader() throws Exception {
    final UserClassLoader.ValidatorClassLoaderPair pair = createPair();
    final Class<?> viaStudentLoader = pair.getStudentLoader().loadClass("StudentMain");
    final Class<?> viaValidationLoader = pair.getValidationLoader().loadClass("StudentMain");
    assertSame(viaStudentLoader, viaValidationLoader);
    assertSame(pair.getStudentLoader(), viaStudentLoader.getClassLoader());
  }

  @Test
  public void validationClassesAreDefinedByTheValidationLoader() throws Exception {
    final UserClassLoader.ValidatorClassLoaderPair pair = createPair();
    final Class<?> validation = pair.getValidationLoader().loadClass("Validation");
    assertSame(pair.getValidationLoader(), validation.getClassLoader());
  }

  @Test
  public void validationCodeCanUseReflectionOnStudentClasses() throws Exception {
    final UserClassLoader.ValidatorClassLoaderPair pair = createPair();
    final Class<?> validation = pair.getValidationLoader().loadClass("Validation");
    final Method callFoo = validation.getMethod("callFoo");
    assertEquals(1, callFoo.invoke(null));
  }

  @Test
  public void studentLoaderBlocksValidatorOnlyClasses() {
    final UserClassLoader.ValidatorClassLoaderPair pair = createPair();
    final UserClassLoader studentLoader = pair.getStudentLoader();
    // Student code cannot reference validation classes...
    assertThrows(ClassNotFoundException.class, () -> studentLoader.loadClass("Validation"));
    // ...or reflection (beyond the EasyMock support classes; Method is mocking-allowed)...
    assertThrows(
        ClassNotFoundException.class, () -> studentLoader.loadClass("java.lang.reflect.Field"));
    assertThrows(
        ClassNotFoundException.class, () -> studentLoader.loadClass("java.lang.reflect.Proxy"));
    // ...or the validation API.
    assertThrows(
        ClassNotFoundException.class,
        () -> studentLoader.loadClass("org.code.validation.ValidationHelper"));
  }

  @Test
  public void validationLoaderAllowsValidatorOnlyClasses() throws Exception {
    final UserClassLoader.ValidatorClassLoaderPair pair = createPair();
    final UserClassLoader validationLoader = pair.getValidationLoader();
    assertNotNull(validationLoader.loadClass("java.lang.reflect.Field"));
    assertNotNull(validationLoader.loadClass("org.code.validation.ValidationHelper"));
  }

  @Test
  public void mockingSupportExistsOnlyInsideAValidatorPair() throws Exception {
    final UserClassLoader.ValidatorClassLoaderPair pair = createPair();
    // The student half of a pair may link EasyMock (needed for mock subclasses of student
    // classes)...
    assertNotNull(pair.getStudentLoader().loadClass("org.easymock.EasyMock"));
    // ...but a standalone USER-level loader (regular run) may not.
    final UserClassLoader standaloneUserLoader =
        new UserClassLoader(
            classLoaderUrls,
            UserClassLoaderTest.class.getClassLoader(),
            STUDENT_CLASSES,
            RunPermissionLevel.USER);
    assertThrows(
        ClassNotFoundException.class,
        () -> standaloneUserLoader.loadClass("org.easymock.EasyMock"));
  }

  @Test
  public void pairSharesOneApprovedClassLoader() {
    final UserClassLoader.ValidatorClassLoaderPair pair = createPair();
    assertSame(
        pair.getStudentLoader().getApprovedClassLoader(),
        pair.getValidationLoader().getApprovedClassLoader());
  }

  @Test
  public void validationCodeCanMockStudentClassesWithEasyMock() throws Exception {
    final UserClassLoader.ValidatorClassLoaderPair pair = createPair();
    final Class<?> mockingValidation = pair.getValidationLoader().loadClass("MockingValidation");
    final Method mockFoo = mockingValidation.getMethod("mockFoo");
    assertEquals(42, mockFoo.invoke(null));
  }

  @Test
  public void closingThePairClosesBothLoaders() throws Exception {
    final UserClassLoader.ValidatorClassLoaderPair pair = createPair();
    pair.close();
    // A closed URLClassLoader can no longer load new classes from its file URLs.
    assertThrows(
        ClassNotFoundException.class, () -> pair.getStudentLoader().loadClass("StudentMain"));
    assertThrows(
        ClassNotFoundException.class, () -> pair.getValidationLoader().loadClass("Validation"));
  }
}
