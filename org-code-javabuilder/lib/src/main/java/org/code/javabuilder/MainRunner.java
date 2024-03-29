package org.code.javabuilder;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.List;
import org.code.javabuilder.util.ProjectLoadUtils;
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
   * @return true if there was code to run, false if there was not.
   */
  public boolean run(URLClassLoader urlClassLoader)
      throws JavabuilderException, InternalFacingException {
    try {
      // Preload error handling classes in case a user project uses up all resources
      Class.forName(UserInitiatedExceptionKey.class.getName());
      Class.forName(UserInitiatedException.class.getName());
    } catch (ClassNotFoundException e) {
      // This shouldn't be possible. If it happens, we should throw lots of errors.
      throw new InternalServerException(InternalExceptionKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }

    try {
      // load and run the main method of the class
      Method mainMethod = ProjectLoadUtils.findMainMethod(urlClassLoader, this.javaFiles);
      if (mainMethod == null) {
        throw new UserInitiatedException(UserInitiatedExceptionKey.NO_MAIN_METHOD);
      }
      this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.RUNNING));
      mainMethod.invoke(null, new Object[] {null});
      return true;
    } catch (IllegalAccessException e) {
      // TODO: this error message may not be not very friendly
      throw new UserInitiatedException(UserInitiatedExceptionKey.ILLEGAL_METHOD_ACCESS, e);
    } catch (InvocationTargetException e) {
      // If the invocation exception is wrapping a known exception type, we don't need to wrap it in
      // a UserInitiatedException
      if (e.getCause() instanceof JavabuilderException) {
        throw (JavabuilderException) e.getCause();
      }
      if (e.getCause() instanceof JavabuilderRuntimeException) {
        throw (JavabuilderRuntimeException) e.getCause();
      }
      if (e.getCause() instanceof InternalFacingException) {
        throw (InternalFacingException) e.getCause();
      }
      if (e.getCause() instanceof InternalFacingRuntimeException) {
        throw (InternalFacingRuntimeException) e.getCause();
      }
      if (e.getCause() instanceof JavabuilderError) {
        throw (JavabuilderError) e.getCause();
      }
      // FileNotFoundExceptions may be thrown from student code, so we treat them as a
      // specific case of a UserInitiatedException
      if (e.getCause() instanceof FileNotFoundException) {
        throw new UserInitiatedException(UserInitiatedExceptionKey.FILE_NOT_FOUND, e.getCause());
      }
      // NoClassDefFoundError is thrown by the class loader if the user attempts to use a disallowed
      // class.
      if (e.getCause() instanceof NoClassDefFoundError) {
        ProjectLoadUtils.convertAndThrowInvalidClassException((NoClassDefFoundError) e.getCause());
      }
      throw new UserInitiatedException(UserInitiatedExceptionKey.RUNTIME_ERROR, e);
    }
  }
}
