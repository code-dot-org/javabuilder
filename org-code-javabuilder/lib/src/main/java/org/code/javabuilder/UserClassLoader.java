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

    // Log that we are going to throw an exception
    JSONObject eventData = new JSONObject();
    eventData.put("type", "invalidClass");
    eventData.put("className", name);
    Logger.getLogger(MAIN_LOGGER).warning(eventData.toString());
    // For now, don't throw an exception, and instead go on to the approved class loader.
    // We want to ensure we aren't blocking anything needed by users, so we are running this in
    // silent mode.
    // throw new ClassNotFoundException(name);
    return this.approvedClassLoader.loadClass(name);
  }

  // Allowed individual classes.
  private static final Set<String> allowedClasses =
      Set.of(
          "java.lang.ArithmeticException",
          "java.lang.ArrayIndexOutOfBoundsException",
          "java.lang.Comparable",
          "java.lang.Double",
          "java.lang.Exception",
          "java.lang.IndexOUtOfBoundsException",
          "java.lang.Integer",
          "java.lang.invoke.StringConcatFactory", // needed for any String concatenation
          "java.lang.IllegalArgumentException",
          "java.lang.Math",
          "java.lang.NullPointerException",
          "java.lang.Object",
          "java.lang.SecurityException",
          "java.lang.String",
          "java.lang.System",
          "java.lang.Throwable");

  // Allowed packages (any individual class is allowed from these classes)
  private static final String[] allowedPackages =
      new String[] {
        "java.io.",
        "java.util.",
        "org.junit.jupiter.api.",
        "org.code.media.",
        "org.code.neighborhood.",
        "org.code.playground.",
        "org.code.theater.",
      };
}
