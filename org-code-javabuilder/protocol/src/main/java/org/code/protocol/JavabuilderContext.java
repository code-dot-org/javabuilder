package org.code.protocol;

import java.util.HashMap;
import java.util.Map;

public class JavabuilderContext {
  private static JavabuilderContext contextInstance;
  private Map<Class, JavabuilderSharedObject> sharedObjects;

  private JavabuilderContext() {
    this.sharedObjects = new HashMap<>();
  }

  public static void create() {
    contextInstance = new JavabuilderContext();
  }

  public static JavabuilderContext getInstance() {
    if (contextInstance == null) {
      JavabuilderContext.create();
    }
    return contextInstance;
  }

  public void destroyAndReset() {
    for(JavabuilderSharedObject sharedObject: sharedObjects.values()) {
      sharedObject.destroy();
    }
    this.sharedObjects = new HashMap<>();
  }

  public void register(Class objectClass, JavabuilderSharedObject sharedObject) {
    this.sharedObjects.put(objectClass, sharedObject);
  }

  public JavabuilderSharedObject get(Class objectClass) {
    if (this.sharedObjects.containsKey(objectClass)) {
      return this.sharedObjects.get(objectClass);
    }
    return null;
  }

  // Convenience method for getting Global Protocol, if it exists.
  public GlobalProtocol getGlobalProtocol() {
    if (this.sharedObjects.containsKey(GlobalProtocol.class)) {
      return (GlobalProtocol) this.sharedObjects.get(GlobalProtocol.class);
    }
    return null;
  }
}
