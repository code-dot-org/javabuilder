package org.code.protocol;

import java.util.HashMap;
import java.util.Map;

public class JavabuilderContext {
  private static JavabuilderContext contextInstance;
  private Map<Class, Object> singletons;

  private JavabuilderContext() {
    this.singletons = new HashMap<>();
  }

  public static void create() {
    contextInstance = new JavabuilderContext();
  }

  public static JavabuilderContext getInstance() {
    return contextInstance;
  }

  public void reset() {
    this.singletons = new HashMap<>();
  }

  public void destroy() {
    this.reset();
  }

  public void registerSingleton(Class singletonClass, Object singleton) {
    this.singletons.put(singletonClass, singleton);
  }

  public Object getSingleton(Class singletonClass) {
    if (this.singletons.containsKey(singletonClass)) {
      return this.singletons.get(singletonClass);
    }
    return null;
  }
}
