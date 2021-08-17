package org.code.javabuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.InternalJavabuilderError;

/** A set of static utility functions that are used in multiple locations */
public class Util {
  private static final String NEIGHBORHOOD_JAR = "neighborhood-full.jar";
  private static final String THEATER_JAR = "theater-full.jar";
  private static final String PLAYGROUND_JAR = "playground-full.jar";

  /** @return a URL describing the location the given jar */
  private static URL getJarURL(String jarName) {
    return Util.class.getClassLoader().getResource("jars/" + jarName);
  }

  /** @return a list of URLs with the location of all user-facing api jars */
  public static URL[] getAllJarURLs(URL executableLocation) {
    return new URL[] {
      executableLocation,
      Util.getJarURL(NEIGHBORHOOD_JAR),
      Util.getJarURL(THEATER_JAR),
      Util.getJarURL(PLAYGROUND_JAR)
    };
  }

  /** @return a joined list of the paths of all user-facing api jars */
  public static String getAllJarPaths() {
    ArrayList<String> allJarPaths = new ArrayList<>();
    try {
      allJarPaths.add(Paths.get(Util.getJarURL(NEIGHBORHOOD_JAR).toURI()).toString());
      allJarPaths.add(Paths.get(Util.getJarURL(THEATER_JAR).toURI()).toString());
      allJarPaths.add(Paths.get(Util.getJarURL(PLAYGROUND_JAR).toURI()).toString());
    } catch (URISyntaxException e) {
      throw new InternalJavabuilderError(InternalErrorKey.INTERNAL_COMPILER_EXCEPTION, e);
    }

    return String.join(System.getProperty("path.separator"), allJarPaths);
  }

  public static void recursivelyClearDirectory(Path directory) throws IOException {
    Files.walk(directory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }
}
