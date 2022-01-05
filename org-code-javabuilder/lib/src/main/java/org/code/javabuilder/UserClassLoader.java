package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * Custom class loader for user-provided code. This class loader only allows certain classes to be
 * used within a user-provided class.
 */
public class UserClassLoader extends URLClassLoader {
  private final Set<String> userProvidedClasses;
  private final URLClassLoader approvedClassLoader;

  public UserClassLoader(URL[] urls, ClassLoader parent, List<String> userProvidedClasses) {
    super(urls, parent);
    this.userProvidedClasses = new HashSet<>();
    this.userProvidedClasses.addAll(userProvidedClasses);
    this.approvedClassLoader = new URLClassLoader(urls, JavaRunner.class.getClassLoader());
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    // System.out.println("loading class " + name);
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
    // allow .<specific-class> usage from valid packages
    for (int i = 0; i < this.allowedPackages.length; i++) {
      if (name.startsWith(this.allowedPackages[i])) {
        return this.approvedClassLoader.loadClass(name);
      }
    }

    // Log that we are going to throw an exception. Log as a warning as it is most likely user
    // error,
    // but we want to track it.
    JSONObject eventData = new JSONObject();
    eventData.put("type", "invalidClass");
    eventData.put("className", name);
    Logger.getLogger(MAIN_LOGGER).warning(eventData.toString());
    throw new ClassNotFoundException(name);
  }

  // Allowed individual classes.
  private static final Set<String> allowedClasses =
      Set.of(
          "java.lang.ArithmeticException",
          "java.lang.ArrayIndexOutOfBoundsException",
          "java.lang.Boolean",
          "java.lang.Byte",
          "java.lang.Character",
          "java.lang.CharSequence",
          "java.lang.Class",
          "java.lang.Comparable",
          "java.lang.Double",
          "java.lang.Enum",
          "java.lang.Exception",
          "java.lang.Float",
          "java.lang.IndexOutOfBoundsException",
          "java.lang.Integer",
          "java.lang.invoke.LambdaMetafactory", // needed if you want to create a lambda function
          "java.lang.invoke.StringConcatFactory", // needed for any String concatenation
          "java.lang.IllegalArgumentException",
          "java.lang.Long",
          "java.lang.Math",
          "java.lang.NullPointerException",
          "java.lang.Number",
          "java.lang.Object",
          "java.lang.RuntimeException",
          "java.lang.SecurityException",
          "java.lang.Short",
          "java.lang.StackTraceElement",
          "java.lang.String",
          "java.lang.StringBuffer",
          "java.lang.StringBuilder",
          "java.lang.System",
          "java.lang.Throwable");

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
      };
}
