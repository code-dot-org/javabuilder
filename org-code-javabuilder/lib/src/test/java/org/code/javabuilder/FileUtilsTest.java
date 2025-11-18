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

  @Test
  public void testIsInRestrictedPackage_javaLang() {
    assertTrue(FileUtils.isInRestrictedPackage("java.lang.Runtime"));
    assertTrue(FileUtils.isInRestrictedPackage("java.lang.Process"));
    assertTrue(FileUtils.isInRestrictedPackage("java.lang.System"));
    assertTrue(FileUtils.isInRestrictedPackage("java.lang.ClassLoader"));
  }

  @Test
  public void testIsInRestrictedPackage_javaNet() {
    assertTrue(FileUtils.isInRestrictedPackage("java.net.Socket"));
    assertTrue(FileUtils.isInRestrictedPackage("java.net.URL"));
    assertTrue(FileUtils.isInRestrictedPackage("java.net.URLConnection"));
  }

  @Test
  public void testIsInRestrictedPackage_javaNio() {
    assertTrue(FileUtils.isInRestrictedPackage("java.nio.file.Files"));
    assertTrue(FileUtils.isInRestrictedPackage("java.nio.file.Paths"));
  }

  @Test
  public void testIsInRestrictedPackage_javaSecurity() {
    assertTrue(FileUtils.isInRestrictedPackage("java.security.Security"));
    assertTrue(FileUtils.isInRestrictedPackage("java.security.Policy"));
  }

  @Test
  public void testIsInRestrictedPackage_javax() {
    assertTrue(FileUtils.isInRestrictedPackage("javax.script.ScriptEngine"));
    assertTrue(FileUtils.isInRestrictedPackage("javax.naming.Context"));
  }

  @Test
  public void testIsInRestrictedPackage_sun() {
    assertTrue(FileUtils.isInRestrictedPackage("sun.misc.Unsafe"));
    assertTrue(FileUtils.isInRestrictedPackage("com.sun.jndi.dns.DnsContext"));
  }

  @Test
  public void testIsInRestrictedPackage_jdk() {
    assertTrue(FileUtils.isInRestrictedPackage("jdk.internal.misc.Unsafe"));
  }

  @Test
  public void testIsInRestrictedPackage_allowedClasses() {
    // These are valid user class names that should NOT be restricted
    assertFalse(FileUtils.isInRestrictedPackage("MyClass"));
    assertFalse(FileUtils.isInRestrictedPackage("com.example.MyClass"));
    assertFalse(FileUtils.isInRestrictedPackage("org.example.MyClass"));
    assertFalse(FileUtils.isInRestrictedPackage("mypackage.MyClass"));
    assertFalse(FileUtils.isInRestrictedPackage("java.MyClass")); // Not java.*, just "java"
    assertFalse(FileUtils.isInRestrictedPackage("javax")); // Not javax.*, just "javax"
  }
}
