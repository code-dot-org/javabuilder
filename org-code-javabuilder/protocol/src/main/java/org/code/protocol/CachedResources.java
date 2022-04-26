package org.code.protocol;

import java.awt.Font;
import java.util.Map;

// Resources we want to use across multiple invocations of Javabuilder.
public class CachedResources {
  private static CachedResources cachedResourcesInstance;
  private Map<String, Font> fontMap;

  public static void create() {
    CachedResources.cachedResourcesInstance = new CachedResources();
  }

  public static CachedResources getInstance() {
    if (CachedResources.cachedResourcesInstance == null) {
      Throwable cause = new IllegalStateException("Cached resources not found.");
      throw new InternalServerRuntimeException(InternalExceptionKey.INTERNAL_EXCEPTION, cause);
    }
    return CachedResources.cachedResourcesInstance;
  }

  public Map<String, Font> getFontMap() {
    if (this.fontMap == null) {
      this.fontMap = FontLoader.getFontMap();
    }
    return this.fontMap;
  }
}
