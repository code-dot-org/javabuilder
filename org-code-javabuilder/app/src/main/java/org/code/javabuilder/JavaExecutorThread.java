package org.code.javabuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Principal;

public class JavaExecutorThread extends Thread {
  URL filePath;
  UserProgram userProgram;
  Principal principal;
  private CompileRunService compileRunService;

  JavaExecutorThread(
      URL filePath,
      UserProgram userProgram,
      Principal principal,
      CompileRunService compileRunService) {
    this.filePath = filePath;
    this.userProgram = userProgram;
    this.principal = principal;
    this.compileRunService = compileRunService;
  }

  public void run() {
    URL[] classLoaderUrls = new URL[] {this.filePath};

    // Create a new URLClassLoader
    URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);

    try {
      // load and run the main method of the class
      urlClassLoader
          .loadClass(this.userProgram.getClassName())
          .getDeclaredMethod("main", new Class[] {String[].class})
          .invoke(null, new Object[] {null});

    } catch (ClassNotFoundException e) {
      // this should be caught earlier in compilation
      System.err.println("Class not found: " + e);
    } catch (NoSuchMethodException e) {
      this.compileRunService.sendMessages(
          this.principal.getName(), "Error: your program does not contain a main method");
    } catch (IllegalAccessException e) {
      // TODO: this error message may not be not very friendly
      this.compileRunService.sendMessages(this.principal.getName(), "Illegal access: " + e);
    } catch (InvocationTargetException e) {
      this.compileRunService.sendMessages(
          this.principal.getName(),
          "Your code hit an exception " + e.getCause().getClass().toString());
    }
    try {
      urlClassLoader.close();
    } catch (IOException e) {
      System.err.println("Error closing urlClassLoader: " + e);
    }
  }
}
