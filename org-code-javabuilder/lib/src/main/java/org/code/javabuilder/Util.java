package org.code.javabuilder;

import java.net.URL;

/** A set of static utility functions that are used in multiple locations */
public class Util {
  /** @return a URL describing the location of the neighborhood jar */
  public static URL getNeighborhoodJar() {
    return Util.class.getClassLoader().getResource("neighborhood-full.jar");
  }
}
