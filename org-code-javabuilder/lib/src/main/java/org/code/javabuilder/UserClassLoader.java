package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.json.JSONObject;

public class UserClassLoader extends URLClassLoader {
  private final List<String> userProvidedClasses;
  private final URLClassLoader approvedClassLoader;

  private static final List<String> allowedClasses =
      new ArrayList<>(
          Arrays.asList(
              "java.io.File",
              "java.io.IOException",
              "java.io.PrintStream",
              "java.io.FileNotFoundException",
              "java.lang.Object",
              "java.lang.Integer",
              "java.lang.Double",
              "java.lang.String",
              "java.lang.Math",
              "java.lang.Comparable",
              "java.lang.Throwable",
              "java.lang.Exception",
              "java.lang.ArithmeticException",
              "java.lang.NullPointerException",
              "java.lang.IndexOUtOfBoundsException",
              "java.lang.ArrayIndexOutOfBoundsException",
              "java.lang.IllegalArgumentException",
              "java.lang.SecurityException",
              "java.lang.System",
              "java.lang.invoke.StringConcatFactory" // needed for any String concatenation
              ));
  private static final String[] allowedPackages =
      new String[] {
        "org.junit.jupiter.api",
        "java.util",
        "org.code.neighborhood",
        "org.code.playground",
        "org.code.theater",
        "org.code.media"
      };

  public UserClassLoader(URL[] urls, ClassLoader parent, List<String> userProvidedClasses) {
    super(urls, parent);
    this.userProvidedClasses = userProvidedClasses;
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
    // allow .* or .<specific-class> imports from valid packages
    for (int i = 0; i < this.allowedPackages.length; i++) {
      if (name.contains(this.allowedPackages[i])) {
        return this.approvedClassLoader.loadClass(name);
      }
    }

    // Log that we are going to throw an exception
    JSONObject eventData = new JSONObject();
    eventData.put("type", "invalidClass");
    eventData.put("className", name);
    Logger.getLogger(MAIN_LOGGER).warning(eventData.toString());
    throw new ClassNotFoundException(name);
  }
}
