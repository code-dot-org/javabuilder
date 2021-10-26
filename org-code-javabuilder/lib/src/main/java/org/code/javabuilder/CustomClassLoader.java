package org.code.javabuilder;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomClassLoader extends URLClassLoader {
  private final List<String> validClasses;
  private static final String[] validImportableClasses =
      new String[] {
        "java.io.File",
        "org.code.neighborhood",
        "org.code.playground",
        "org.code.theater",
        "org.code.media"
      };
  private static final String[] validPackages = new String[] {"java.util", "java.lang"};

  public CustomClassLoader(URL[] urls, ClassLoader parent, List<String> userProvidedClasses) {
    super(urls, parent);
    this.validClasses = new ArrayList<>(userProvidedClasses);
    this.validClasses.addAll(Arrays.asList(validImportableClasses));
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
      System.out.println("should throw class not found exception for " + name);
      throw new ClassNotFoundException(name);
    }
    return super.loadClass(name);
  }
}
