package org.code.javabuilder;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserClassLoader extends URLClassLoader {
  private final List<String> validClasses;
  private final URLClassLoader approvedClassLoader;

  private static final String[] validImportableClasses =
      new String[] {
        "java.io.File",
        "java.io.IOException",
        "java.io.PrintStream",
        "java.io.FileNotFoundException",
        "org.junit.jupiter.api",
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
      };
  private static final String[] validPackages =
      new String[] {
        "java.util",
        "org.code.neighborhood",
        "org.code.playground",
        "org.code.theater",
        "org.code.media",
        "org.code.protocol"
      };

  public UserClassLoader(URL[] urls, ClassLoader parent, List<String> userProvidedClasses) {
    super(urls, parent);
    this.validClasses = new ArrayList<>(userProvidedClasses);
    this.validClasses.addAll(Arrays.asList(validImportableClasses));
    this.approvedClassLoader = new URLClassLoader(urls);
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    System.out.println("loading class " + name);
    boolean validClass = false;
    if (this.validClasses.contains(name)) {
      validClass = true;
    }
    // allow .* or .<specific-class> imports from valid packages
    for (int i = 0; i < this.validPackages.length; i++) {
      if (name.contains(this.validPackages[i])) {
        validClass = true;
      }
    }
    if (!validClass) {
      System.out.println("about to throw exception for " + name);
      throw new ClassNotFoundException(name);
    }
    return super.loadClass(name);
  }
}
