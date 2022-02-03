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
      Method mainMethod = Util.findMainMethod(urlClassLoader, this.javaFiles);
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
        Util.throwInvalidClassException((NoClassDefFoundError) e.getCause());
      }
      throw new UserInitiatedException(UserInitiatedExceptionKey.RUNTIME_ERROR, e);
    }
  }
}
