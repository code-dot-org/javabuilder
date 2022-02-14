package org.code.javabuilder.util;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.code.javabuilder.InternalServerError;
import org.code.protocol.InternalErrorKey;

public final class JarUtils {
  private JarUtils() {
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
    return JarUtils.class.getClassLoader().getResource("jars/" + jarName);
  }

  /** @return a list of URLs with the location of all user-facing api jars */
  public static URL[] getAllJarURLs(URL executableLocation) {
    final URL[] jarUrls = new URL[JAR_NAMES.length + 1];
    jarUrls[0] = executableLocation;
    for (int i = 0; i < JAR_NAMES.length; i++) {
      jarUrls[i + 1] = JarUtils.getJarURL(JAR_NAMES[i]);
    }

    return jarUrls;
  }

  /** @return a joined list of the paths of all user-facing api jars */
  public static String getAllJarPaths() throws InternalServerError {
    ArrayList<String> allJarPaths = new ArrayList<>();
    try {
      for (String jarName : JAR_NAMES) {
        allJarPaths.add(Paths.get(JarUtils.getJarURL(jarName).toURI()).toString());
      }
    } catch (URISyntaxException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_COMPILER_EXCEPTION, e);
    }

    return String.join(System.getProperty("path.separator"), allJarPaths);
  }
}
