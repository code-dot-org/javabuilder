package org.code.javabuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/** The class that executes the student's code */
public class JavaRunner {
  private final File executableLocation;
  private final ProjectFileManager fileManager;
  private final OutputAdapter outputAdapter;
  private final InputAdapter inputAdapter;

  public JavaRunner(
      File executableLocation,
      ProjectFileManager fileManager,
      OutputAdapter outputAdapter,
      InputAdapter inputAdapter) {
    this.executableLocation = executableLocation;
    this.fileManager = fileManager;
    this.outputAdapter = outputAdapter;
    this.inputAdapter = inputAdapter;
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
    URL[] classLoaderUrls;
    try {
      classLoaderUrls = new URL[] {this.executableLocation.toURI().toURL()};
    } catch (MalformedURLException e) {
      throw new UserFacingException(
          "We hit an error on our side while running your program. Try Again", e);
    }

    // Create a new URLClassLoader
    URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);

    String mainClass = this.findMainClass(urlClassLoader);
    runMainClass(mainClass);

    try {
      urlClassLoader.close();
    } catch (IOException e) {
      // The user code has finished running. We don't want to confuse them at this point with an
      // error message.
      throw new InternalFacingException("Error closing urlClassLoader: " + e, e);
    }
  }

  private void runMainClass(String mainClass) throws UserFacingException {
    ProcessBuilder processBuilder = new ProcessBuilder("java", mainClass);
    processBuilder.directory(this.executableLocation);
    processBuilder.redirectErrorStream(true);
    Process process;
    try {
      process = processBuilder.start();
    } catch (IOException e) {
      throw new UserFacingException(
          "We hit an error on our side while running your program. Try Again", e);
    }
    OutputStream stdin = process.getOutputStream();
    InputStream stdout = process.getInputStream();

    InputStream userInput = new InputRedirectionStream(this.inputAdapter);
    OutputStream outputToUser = new OutputPrintStream(this.outputAdapter);

    Thread inputThread = new Thread(new InputProcessor(userInput, stdin));
    Thread outputThread = new Thread(new InputProcessor(stdout, outputToUser));
    inputThread.start();
    outputThread.start();
    try {
      process.waitFor();
    } catch (InterruptedException e) {
      throw new UserFacingException(
          "We hit an error on our side while running your program. Try Again", e);
    }
    inputThread.interrupt();
    outputThread.interrupt();
  }

  /**
   * Finds the name of the Class with a main method in the set of files in fileManager if it exists.
   *
   * @param urlClassLoader class loader pointing to location of compiled classes
   * @return the name of the Class if it is found
   * @throws UserFacingException if there is an issue loading a class
   * @throws UserInitiatedException if there is more than one main method or no main method
   */
  public String findMainClass(URLClassLoader urlClassLoader)
      throws UserFacingException, UserInitiatedException {

    String mainClass = null;
    List<JavaProjectFile> fileList = this.fileManager.getJavaFiles();
    for (JavaProjectFile file : fileList) {
      try {
        Method[] declaredMethods =
            urlClassLoader.loadClass(file.getClassName()).getDeclaredMethods();
        for (Method method : declaredMethods) {
          Class[] parameterTypes = method.getParameterTypes();
          if (method.getName().equals("main")
              && parameterTypes.length == 1
              && parameterTypes[0].equals(String[].class)) {
            if (mainClass != null) {
              throw new UserInitiatedException(
                  "Your code can only have one main method. We found at least two classes with main methods.");
            }
            mainClass = file.getClassName();
          }
        }
      } catch (ClassNotFoundException e) {
        // this should be caught earlier in compilation
        throw new UserFacingException(
            "We hit an error on our side while running your program. Try Again", e);
      }
    }

    if (mainClass == null) {
      throw new UserInitiatedException("Error: your program does not contain a main method");
    }
    return mainClass;
  }
}
