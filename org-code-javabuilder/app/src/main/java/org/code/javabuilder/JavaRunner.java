package org.code.javabuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.security.Principal;
import java.util.Arrays;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.springframework.stereotype.Component;

@Component
public class JavaRunner {

  private CompileRunService compileRunService;

  public JavaRunner(CompileRunService compileRunService) {
    this.compileRunService = compileRunService;
  }

  /**
   * Compile and Run the given user program and output results to this.compileRunService for the
   * given user (principal)
   *
   * @param userProgram
   * @param principal
   */
  public void compileAndRunUserProgram(UserProgram userProgram, Principal principal) {

    if (userProgram.getFileName().indexOf(".java") > 0) {
      userProgram.setClassName(
          userProgram.getFileName().substring(0, userProgram.getFileName().indexOf(".java")));
    } else {
      this.compileRunService.sendMessages(
          principal.getName(), "Invalid File Name. File name must end in '.java'.");
      return;
    }

    File tempFolder = null;
    try {
      tempFolder = Files.createTempDirectory("tmpdir").toFile();

      this.compileRunService.sendMessages(principal.getName(), "Compiling your program...");
      boolean compileSuccess = compileProgram(userProgram, principal, tempFolder);

      if (compileSuccess) {
        // this try statement will close the streams automatically
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(outputStream)) {
          // set System.out to be a specific output stream in order to capture output of the
          // program and send it back to the user
          System.setOut(out);

          this.compileRunService.sendMessages(principal.getName(), "Compiled!");
          this.compileRunService.sendMessages(principal.getName(), "Running your program...");
          runClass(tempFolder.toURI().toURL(), userProgram, principal);

          // outputStream should now contain output of userProgram
          outputStream.flush();
          String result = outputStream.toString();
          if (result.length() > 0) {
            this.compileRunService.sendMessages(principal.getName(), result);
          }
        }
      } else {
        this.compileRunService.sendMessages(
            principal.getName(), "There was an error compiling your program.");
      }

    } catch (IOException e) {
      // IOException could be called by creating a temporary folder or writing to that folder.
      // May need better error handling for this.
      this.compileRunService.sendMessages(
          principal.getName(), "There was an issue trying to run your program, please try again.");
      e.printStackTrace();
    }

    if (tempFolder != null) {
      tempFolder.delete();
    }

    // ensure System.out is reset
    System.setOut(System.out);
  }

  private boolean compileProgram(UserProgram userProgram, Principal principal, File tempFolder) {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

    CompilationTask task = getCompilationTask(userProgram, tempFolder, diagnostics);
    if (task == null) {
      return false;
    }

    boolean success = task.call();

    // diagnostics will include any compiler errors
    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
      this.compileRunService.sendMessages(principal.getName(), diagnostic.toString());
    }
    return success;
  }

  // Given a user program, create a compilation task that will save the .class file to the given
  // temp folder and output any compilation messages to diagnostics.
  private CompilationTask getCompilationTask(
      UserProgram userProgram, File tempFolder, DiagnosticCollector<JavaFileObject> diagnostics) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    // set output of compilation to be a temporary folder
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    try {
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(tempFolder));
    } catch (IOException e) {
      e.printStackTrace();
      // if we can't set the file location we won't be able to run the class properly, so return
      // null
      return null;
    }

    // create file for user-provided code
    JavaFileObject file =
        new JavaSourceFromString(userProgram.getClassName(), userProgram.getCode());
    Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);

    // create compilation task
    CompilationTask task =
        compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
    return task;
  }

  private void runClass(URL filePath, UserProgram userProgram, Principal principal) {
    URL[] classLoaderUrls = new URL[] {filePath};

    // Create a new URLClassLoader
    URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);

    try {
      // load and run the main method of the class
      urlClassLoader
          .loadClass(userProgram.getClassName())
          .getDeclaredMethod("main", new Class[] {String[].class})
          .invoke(null, new Object[] {null});

    } catch (ClassNotFoundException e) {
      // this should be caught earlier in compilation
      System.err.println("Class not found: " + e);
    } catch (NoSuchMethodException e) {
      this.compileRunService.sendMessages(
          principal.getName(), "Error: your program does not contain a main method");
    } catch (IllegalAccessException e) {
      // TODO: this error message may not be not very friendly
      this.compileRunService.sendMessages(principal.getName(), "Illegal access: " + e);
    } catch (InvocationTargetException e) {
      this.compileRunService.sendMessages(
          principal.getName(), "Your code hit an exception " + e.getCause().getClass().toString());
    }
    try {
      urlClassLoader.close();
    } catch (IOException e) {
      System.err.println("Error closing urlClassLoader: " + e);
    }
  }
}
