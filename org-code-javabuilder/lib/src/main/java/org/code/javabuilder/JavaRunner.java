package org.code.javabuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/** The class that executes the student's code */
public class JavaRunner {
  private final URL executableLocation;
  private final List<JavaProjectFile> javaFiles;

  public JavaRunner(URL executableLocation, List<JavaProjectFile> javaFiles) {
    this.executableLocation = executableLocation;
    this.javaFiles = javaFiles;
  }

  /**
   * Run the compiled user code.
   *
   * @throws UserFacingException When the user's code hits a runtime error or fails due to an
   *     internal error.
   * @throws InternalFacingException When we hit an internal error after the user's code has
   *     finished executing.
   */
  public void runCode()
      throws UserFacingException, InternalFacingException, UserInitiatedException {
    // Include the neighborhood classes in the code we are loading so student code can access them.
    URL[] classLoaderUrls = new URL[] {this.executableLocation, Util.getNeighborhoodJar()};

    // Create a new URLClassLoader
    URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);

    try {
      // load and run the main method of the class
      Method mainMethod = this.findMainMethod(urlClassLoader);
      mainMethod.invoke(null, new Object[] {null});
    } catch (IllegalAccessException e) {
      // TODO: this error message may not be not very friendly
      throw new UserInitiatedException(UserInitiatedExceptionKey.ILLEGAL_METHOD_ACCESS, e);
    } catch (InvocationTargetException e) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.RUNTIME_ERROR, e);
    }
    try {
      urlClassLoader.close();
    } catch (IOException e) {
      // The user code has finished running. We don't want to confuse them at this point with an
      // error message.
      throw new InternalFacingException("Error closing urlClassLoader: " + e, e);
    }
  }

  /**
   * Finds the main method in the set of files in fileManager if it exists.
   *
   * @param urlClassLoader class loader pointing to location of compiled classes
   * @return the main method if it is found
   * @throws UserFacingException if there is an issue loading a class
   * @throws UserInitiatedException if there is more than one main method or no main method
   */
  public Method findMainMethod(URLClassLoader urlClassLoader)
      throws UserFacingException, UserInitiatedException {

    Method mainMethod = null;
    for (JavaProjectFile file : this.javaFiles) {
      try {
        Method[] declaredMethods =
            urlClassLoader.loadClass(file.getClassName()).getDeclaredMethods();
        for (Method method : declaredMethods) {
          Class[] parameterTypes = method.getParameterTypes();
          if (method.getName().equals("main")
              && parameterTypes.length == 1
              && parameterTypes[0].equals(String[].class)) {
            if (mainMethod != null) {
              throw new UserInitiatedException(UserInitiatedExceptionKey.TWO_MAIN_METHODS);
            }
            mainMethod = method;
          }
        }
      } catch (ClassNotFoundException e) {
        // this should be caught earlier in compilation
        throw new UserFacingException(UserFacingExceptionKey.INTERNAL_RUNTIME_EXCEPTION, e);
      }
    }

    if (mainMethod == null) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.NO_MAIN_METHOD);
    }
    return mainMethod;
  }
}
