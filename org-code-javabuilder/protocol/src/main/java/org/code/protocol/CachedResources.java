package org.code.protocol;

import java.awt.Font;
import java.util.Map;

// Resources we want to use across multiple invocations of Javabuilder.
public class CachedResources {
  private Map<String, Font> fontMap;

  public Map<String, Font> getFontMap() {
    if (this.fontMap == null) {
      this.fontMap = FontLoader.getFontMap();
    }
    return this.fontMap;
  }
}
