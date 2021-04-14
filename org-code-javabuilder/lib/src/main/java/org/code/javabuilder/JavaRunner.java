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
  private final ProjectFileManager fileManager;
  private final OutputAdapter outputAdapter;

  public JavaRunner(
      URL executableLocation, ProjectFileManager fileManager, OutputAdapter outputAdapter) {
    this.executableLocation = executableLocation;
    this.fileManager = fileManager;
    this.outputAdapter = outputAdapter;
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
    URL[] classLoaderUrls = new URL[] {this.executableLocation};

    // Create a new URLClassLoader
    URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);

    try {
      // load and run the main method of the class
      Method mainMethod = this.findMainMethod(urlClassLoader);
      mainMethod.invoke(null, new Object[] {null});
    } catch (IllegalAccessException e) {
      // TODO: this error message may not be not very friendly
      throw new UserFacingException("Illegal access: " + e, e);
    } catch (InvocationTargetException e) {
      throw new UserInitiatedException(
          "Your code hit an exception " + e.getCause().getClass().toString(), e);
    }
    try {
      urlClassLoader.close();
    } catch (IOException e) {
      // The user code has finished running. We don't want to confuse them at this point with an
      // error message.
      throw new InternalFacingException("Error closing urlClassLoader: " + e, e);
    }
  }

  private Method findMainMethod(URLClassLoader classLoader)
      throws UserFacingException, UserInitiatedException {
    List<ProjectFile> files = this.fileManager.getFiles();
    Method mainMethod = null;
    for (int i = 0; i < files.size(); i++) {
      ProjectFile file = files.get(i);
      try {
        Method tempMain =
            classLoader.loadClass(file.getClassName()).getDeclaredMethod("main", String[].class);
        if (mainMethod != null) {
          throw new UserInitiatedException(
              "Your code can only have one main method."
                  + " We found at least two classes with main methods.");
        } else {
          mainMethod = tempMain;
        }
      } catch (ClassNotFoundException e) {
        // this should be caught earlier in compilation
        throw new UserFacingException(
            "We hit an error on our side while running your program. Try Again", e);
      } catch (NoSuchMethodException e) {
        // this class does not have a main, this exception can be safely ignored.
      }
    }
    if (mainMethod == null) {
      throw new UserInitiatedException("Error: your program does not contain a main method");
    }
    return mainMethod;
  }
}
