package org.code.javabuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.LoggerUtils;

/** A set of static utility functions that are used in multiple locations */
public final class Util {
  private Util() {
    throw new UnsupportedOperationException("Instantiation of utility class is not allowed.");
  }

  private static final String[] JAR_NAMES = {
    "neighborhood-full.jar",
    "theater-full.jar",
    "playground-full.jar",
    "studentlib-full.jar",
    "validation-full.jar"
  };

  /** @return a URL describing the location the given jar */
  private static URL getJarURL(String jarName) {
    return Util.class.getClassLoader().getResource("jars/" + jarName);
  }

  /** @return a list of URLs with the location of all user-facing api jars */
  public static URL[] getAllJarURLs(URL executableLocation) {
    final URL[] jarUrls = new URL[JAR_NAMES.length + 1];
    jarUrls[0] = executableLocation;
    for (int i = 0; i < JAR_NAMES.length; i++) {
      jarUrls[i + 1] = Util.getJarURL(JAR_NAMES[i]);
    }

    return jarUrls;
  }

  /** @return a joined list of the paths of all user-facing api jars */
  public static String getAllJarPaths() throws InternalServerError {
    ArrayList<String> allJarPaths = new ArrayList<>();
    try {
      for (String jarName : JAR_NAMES) {
        allJarPaths.add(Paths.get(Util.getJarURL(jarName).toURI()).toString());
      }
    } catch (URISyntaxException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_COMPILER_EXCEPTION, e);
    }

    return String.join(System.getProperty("path.separator"), allJarPaths);
  }

  public static void recursivelyClearDirectory(Path directory) throws IOException {
    Files.walk(directory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    LoggerUtils.sendClearedDirectoryLog(directory);
  }

  public static void throwInvalidClassException(NoClassDefFoundError e)
      throws UserInitiatedException {
    String message = "";
    if (e.getMessage() != null) {
      // the message will be the name of the invalid class, with '.' replaced by '/'.
      message = e.getMessage().replace('/', '.');
    }
    ClassNotFoundException classNotFoundException = new ClassNotFoundException(message);
    throw new UserInitiatedException(
        UserInitiatedExceptionKey.INVALID_CLASS, classNotFoundException);
  }

  /**
   * Finds the main method in the set of files in the given list of project files if it exists.
   *
   * @param classLoader class loader pointing to location of compiled classes
   * @param javaFiles a list of JavaProjectFiles
   * @return the main method if it is found
   * @throws UserInitiatedException if there is more than one main method or no main method, or if
   *     the class definition is empty
   */
  public static Method findMainMethod(URLClassLoader classLoader, List<JavaProjectFile> javaFiles)
      throws UserInitiatedException {
    Method mainMethod = null;
    for (JavaProjectFile file : javaFiles) {
      try {
        Method[] declaredMethods = classLoader.loadClass(file.getClassName()).getDeclaredMethods();
        for (Method method : declaredMethods) {
          Class[] parameterTypes = method.getParameterTypes();
          if (method.getName().equals("main")
              && parameterTypes.length == 1
              && parameterTypes[0].equals(String[].class)) {
            if (mainMethod != null) {
              throw new UserInitiatedException(UserInitiatedExceptionKey.TWO_MAIN_METHODS);
            }
            mainMethod = method;
          }
        }
      } catch (ClassNotFoundException e) {
        // May be thrown if file is empty or contains only comments
        throw new UserInitiatedException(UserInitiatedExceptionKey.CLASS_NOT_FOUND, e);
      } catch (NoClassDefFoundError e) {
        Util.throwInvalidClassException(e);
      }
    }

    return mainMethod;
  }
}
