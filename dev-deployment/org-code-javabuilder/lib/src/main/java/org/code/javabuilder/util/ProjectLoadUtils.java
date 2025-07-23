package org.code.javabuilder.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.List;
import org.code.javabuilder.JavaProjectFile;
import org.code.javabuilder.UserInitiatedException;
import org.code.javabuilder.UserInitiatedExceptionKey;

public final class ProjectLoadUtils {
  private ProjectLoadUtils() {
    throw new UnsupportedOperationException("Instantiation of utility class is not allowed.");
  }

  /**
   * Finds the main method in the set of files in the given list of project files if it exists.
   *
   * @param classLoader class loader pointing to location of compiled classes
   * @param javaFiles a list of JavaProjectFiles
   * @return the main method if it is found
   * @throws UserInitiatedException if there is more than one main method, no main method, an error
   *     with the main method signature, or if the class definition is empty
   */
  public static Method findMainMethod(URLClassLoader classLoader, List<JavaProjectFile> javaFiles)
      throws UserInitiatedException {
    Method mainMethod = null;
    for (JavaProjectFile file : javaFiles) {
      try {
        Method[] declaredMethods = classLoader.loadClass(file.getClassName()).getDeclaredMethods();
        for (Method method : declaredMethods) {
          if (method.getName().equals("main")) {
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
        ProjectLoadUtils.convertAndThrowInvalidClassException(e);
      }
    }

    // If we found a main method, make sure the method signature is valid
    // (public, static, void return type, one argument which should be a String[]).
    if (mainMethod != null) {
      final Class<?>[] parameterTypes = mainMethod.getParameterTypes();
      final int modifiers = mainMethod.getModifiers();
      if (!Modifier.isPublic(modifiers)
          || !Modifier.isStatic(modifiers)
          || mainMethod.getGenericReturnType() != Void.TYPE
          || parameterTypes.length != 1
          || !parameterTypes[0].equals(String[].class)) {
        throw new UserInitiatedException(UserInitiatedExceptionKey.INVALID_MAIN_METHOD);
      }
    }

    return mainMethod;
  }

  /**
   * Convert the given NoClassDefFoundError into a UserInitiatedException that contains a
   * ClassNotFoundException. This is needed because our class loader will throw NoClassDefFoundError
   * for an invalid class, but we want to surface this error as a ClassNotFoundException.
   *
   * @param e NoClassDefFoundError
   * @throws UserInitiatedException
   */
  public static void convertAndThrowInvalidClassException(NoClassDefFoundError e)
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
}
