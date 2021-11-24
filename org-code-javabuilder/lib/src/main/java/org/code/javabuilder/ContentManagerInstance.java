package org.code.javabuilder;

// Hack
public class ContentManagerInstance {
  private static ContentManager INSTANCE;

  public static ContentManager getContentManager() {
    return INSTANCE;
  }

  public static void setContentManager(ContentManager contentManager) {
    INSTANCE = contentManager;
  }
}
