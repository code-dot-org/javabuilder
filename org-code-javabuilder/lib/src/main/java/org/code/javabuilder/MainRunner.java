package org.code.javabuilder;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.List;
import org.code.protocol.*;

/** Finds and runs the main method in a given set of Java files */
public class MainRunner implements CodeRunner {
  private final List<JavaProjectFile> javaFiles;
  private final OutputAdapter outputAdapter;

  public MainRunner(List<JavaProjectFile> javaFiles, OutputAdapter outputAdapter) {
    this.javaFiles = javaFiles;
    this.outputAdapter = outputAdapter;
  }

  /**
   * Finds and runs the main method using the given set of Java files and the given URLClassLoader
   *
   * @param urlClassLoader class loader to load compiled classes
   * @throws JavabuilderException when the user's code hits an error
   */
  public void run(URLClassLoader urlClassLoader) throws JavabuilderException {
    try {
      // load and run the main method of the class
      Method mainMethod = this.findMainMethod(urlClassLoader);
      this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.RUNNING));
      mainMethod.invoke(null, new Object[] {null});
    } catch (IllegalAccessException e) {
      // TODO: this error message may not be not very friendly
      throw new UserInitiatedException(UserInitiatedExceptionKey.ILLEGAL_METHOD_ACCESS, e);
    } catch (InvocationTargetException e) {
      // If the invocation exception is wrapping another JavabuilderException or
      // JavabuilderRuntimeException, we don't need to wrap it in a UserInitiatedException
      if (e.getCause() instanceof JavabuilderException) {
        throw (JavabuilderException) e.getCause();
      }
      if (e.getCause() instanceof JavabuilderRuntimeException) {
        throw (JavabuilderRuntimeException) e.getCause();
      }
      // FileNotFoundExceptions may be thrown from student code, so we treat them as a
      // specific case of a UserInitiatedException
      if (e.getCause() instanceof FileNotFoundException) {
        throw new UserInitiatedException(UserInitiatedExceptionKey.FILE_NOT_FOUND, e.getCause());
      }
      // NoClassDefFoundError is thrown by the class loader if the user attempts to use a disallowed
      // class.
      if (e.getCause() instanceof NoClassDefFoundError) {
        this.throwInvalidClassException((NoClassDefFoundError) e.getCause());
      }
      throw new UserInitiatedException(UserInitiatedExceptionKey.RUNTIME_ERROR, e);
    }
  }

  /**
   * Finds the main method in the set of files in fileManager if it exists.
   *
   * @param urlClassLoader class loader pointing to location of compiled classes
   * @return the main method if it is found
   * @throws UserInitiatedException if there is more than one main method or no main method, or if
   *     the class definition is empty
   */
  private Method findMainMethod(URLClassLoader urlClassLoader) throws UserInitiatedException {
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
        // May be thrown if file is empty or contains only comments
        throw new UserInitiatedException(UserInitiatedExceptionKey.CLASS_NOT_FOUND, e);
      } catch (NoClassDefFoundError e) {
        this.throwInvalidClassException(e);
      }
    }

    if (mainMethod == null) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.NO_MAIN_METHOD);
    }
    return mainMethod;
  }

  private void throwInvalidClassException(NoClassDefFoundError e) throws UserInitiatedException {
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
