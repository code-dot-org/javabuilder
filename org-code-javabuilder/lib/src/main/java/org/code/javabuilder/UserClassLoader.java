package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.StringConcatFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.code.protocol.LoggerConstants;
import org.json.JSONObject;

/**
 * Custom class loader for user-provided code. This class loader only allows certain classes to be
 * used within a user-provided class.
 */
public class UserClassLoader extends URLClassLoader {
  private final Set<String> userProvidedClasses;
  private final URLClassLoader approvedClassLoader;
  private final RunPermissionLevel permissionLevel;

  public UserClassLoader(
      URL[] urls,
      ClassLoader parent,
      List<String> userProvidedClasses,
      RunPermissionLevel permissionLevel) {
    super(urls, parent);
    this.userProvidedClasses = new HashSet<>();
    this.userProvidedClasses.addAll(userProvidedClasses);
    this.approvedClassLoader = new URLClassLoader(urls, JavaRunner.class.getClassLoader());
    this.permissionLevel = permissionLevel;
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    // Call super for user provided classes, as we need to verify users are not
    // trying to use an unapproved class or package.
    if (this.userProvidedClasses.contains(name)) {
      return super.loadClass(name);
    }
    // If this is not a user provided class, we are loading something used by a user provided class.
    // If it is either an allowed class or package, we can load with our standard class loader.
    // Otherwise, throw an exception.
    if (this.allowedClasses.contains(name)) {
      return this.approvedClassLoader.loadClass(name);
    }
    // allow .<specific-class> usage from allowed packages. If this code
    // has validation permissions, also check the
    // validator permissions allowed package list.
    if (this.isInAllowedPackage(this.allowedPackages, name)
        || (this.permissionLevel == RunPermissionLevel.VALIDATOR
            && this.isInAllowedPackage(this.validatorAllowedPackages, name))) {
      return this.approvedClassLoader.loadClass(name);
    }

    // Log that we are going to throw an exception. Log as a warning
    // as it is most likely user error, but we want to track it.
    JSONObject eventData = new JSONObject();
    eventData.put(LoggerConstants.TYPE, "invalidClass");
    eventData.put(LoggerConstants.CLASS_NAME, name);
    Logger.getLogger(MAIN_LOGGER).warning(eventData.toString());
    throw new ClassNotFoundException(name);
  }

  /**
   * @param allowedPackageList
   * @param name
   * @return true if name is in a package a in the allowedPackageList, i.e. if name is prefixed with
   *     any value in allowedPackageList
   */
  private boolean isInAllowedPackage(String[] allowedPackageList, String name) {
    for (int i = 0; i < allowedPackageList.length; i++) {
      if (name.startsWith(allowedPackageList[i])) {
        return true;
      }
    }
    return false;
  }

  // Allowed individual classes.
  private static final Set<String> allowedClasses =
      Set.of(
          ArithmeticException.class.getName(),
          ArrayIndexOutOfBoundsException.class.getName(),
          Boolean.class.getName(),
          Byte.class.getName(),
          Character.class.getName(),
          CharSequence.class.getName(),
          Class.class.getName(),
          Comparable.class.getName(),
          Double.class.getName(),
          Enum.class.getName(),
          Exception.class.getName(),
          Float.class.getName(),
          IndexOutOfBoundsException.class.getName(),
          Integer.class.getName(),
          LambdaMetafactory.class.getName(), // needed if you want to create a lambda function
          StringConcatFactory.class.getName(), // needed for any String concatenation
          IllegalArgumentException.class.getName(),
          Long.class.getName(),
          Math.class.getName(),
          NullPointerException.class.getName(),
          Number.class.getName(),
          Object.class.getName(),
          RuntimeException.class.getName(),
          SecurityException.class.getName(),
          Short.class.getName(),
          StackTraceElement.class.getName(),
          String.class.getName(),
          StringBuffer.class.getName(),
          StringBuilder.class.getName(),
          Throwable.class.getName());

  // Allowed packages (any individual class is allowed from these classes)
  private static final String[] allowedPackages =
      new String[] {
        "java.io.",
        "java.math.",
        "java.text.",
        "java.time.",
        "java.util.",
        "org.junit.jupiter.api.",
        "org.code.media.",
        "org.code.neighborhood.",
        "org.code.playground.",
        "org.code.theater.",
        "org.code.lang"
      };

  // Allowed packages for code with elevated permissions, such as validation code.
  private static final String[] validatorAllowedPackages = new String[] {"org.code.validation"};
}
