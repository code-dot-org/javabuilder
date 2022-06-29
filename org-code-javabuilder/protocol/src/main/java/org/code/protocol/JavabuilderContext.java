package org.code.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is meant to keep track of shared singletons that should only exist for the lifetime of
 * one Javabuilder session. Any class can be registered as long as it extends
 * JavabuilderSharedObject, but only one object per class can be registered.
 */
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

  public void onExecutionEnded() {
    for (JavabuilderSharedObject sharedObject : sharedObjects.values()) {
      sharedObject.onExecutionEnded();
    }
  }

  public void destroyAndReset() {
    for (JavabuilderSharedObject sharedObject : sharedObjects.values()) {
      sharedObject.destroy();
    }
    this.sharedObjects = new HashMap<>();
  }

  public boolean containsKey(Class key) {
    return this.sharedObjects.containsKey(key);
  }

  public void register(Class objectClass, JavabuilderSharedObject sharedObject) {
    if (!objectClass.isAssignableFrom(sharedObject.getClass())) {
      String message =
          String.format(
              "Attempting to add a class, %s to JavabuilderContext that cannot be converted to its key, %s",
              sharedObject.getClass(), objectClass);
      throw new IllegalArgumentException(message);
    }
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
