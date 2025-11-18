package org.code.javabuilder.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/** Convenience methods for handling file operations */
public final class FileUtils {
  private FileUtils() {
    throw new UnsupportedOperationException("Instantiation of utility class is not allowed.");
  }

  public static final String JAVA_EXTENSION = ".java";

  /**
   * List of restricted package prefixes that users are not allowed to create classes in. These
   * packages contain system classes that could be used to bypass security restrictions.
   */
  private static final String[] RESTRICTED_PACKAGE_PREFIXES =
      new String[] {
        // Contains core classes that enable remote code execution and privilege escalation
        "java.lang.",
        // Contains networking classes that enable unauthorized network access:
        "java.net.",
        // Contains advanced I/O classes that could bypass file access restrictions:
        "java.nio.",
        // Contains security infrastructure that could disable protections:
        "java.security.",
        // Contains various Java extension packages with security implications:
        "javax.",
        // Internal Sun/Oracle JDK classes that bypass normal Java security:
        "sun.",
        // Internal Sun/Oracle classes with security implications:
        "com.sun.",
        // Internal JDK classes (Java 9+) that provide low-level access:
        "jdk.",
        // XML processing APIs (part of Java SE, blocked for defense in depth):
        "org.xml.",
        // W3C standard APIs bundled with Java (blocked for defense in depth):
        "org.w3c.",
        // IETF standard APIs bundled with Java (blocked for defense in depth):
        "org.ietf.",
        // OMG/CORBA APIs bundled with Java (blocked for defense in depth):
        "org.omg."
      };

  /**
   * Whether the given file name is a valid Java file name. Must end with ".java" and have at least
   * one character before the extension.
   *
   * @param fileName file name to check
   * @return whether the file name is a valid Java file name
   */
  public static boolean isJavaFile(String fileName) {
    return fileName.endsWith(JAVA_EXTENSION) && fileName.indexOf(JAVA_EXTENSION) > 0;
  }

  /**
   * Checks if the given class name is in a restricted package. Users are not allowed to create
   * classes in restricted packages as this could be used to bypass security restrictions.
   *
   * @param className the class name to check
   * @return true if the class name is in a restricted package, false otherwise
   */
  public static boolean isInRestrictedPackage(String className) {
    for (String restrictedPrefix : RESTRICTED_PACKAGE_PREFIXES) {
      if (className.startsWith(restrictedPrefix)) {
        return true;
      }
    }
    return false;
  }

  public static void recursivelyClearDirectory(Path directory) throws IOException {
    Files.walk(directory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }
}
