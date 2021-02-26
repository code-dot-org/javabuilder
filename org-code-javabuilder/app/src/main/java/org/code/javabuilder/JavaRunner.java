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

public class JavaRunner {

  /**
   * Compile and Run the given user program and output results to the given compileRunService for
   * the given user (principal)
   *
   * @param userProgram
   * @param principal
   * @param compileRunService
   */
  public static void compileAndRunUserProgram(
      UserProgram userProgram, Principal principal, CompileRunService compileRunService) {
    String filename = userProgram.getFileName();
    // We expect the filename to have no .java suffix, remove it if necessary.
    if (filename.endsWith(".java")) {
      userProgram.setFileName(filename.substring(0, filename.indexOf(".java")));
    }

    File tempFolder = null;
    try {
      tempFolder = Files.createTempDirectory("tmpdir").toFile();

      compileRunService.sendMessages(principal.getName(), "Compiling your program...");
      boolean compileSuccess =
          compileProgram(userProgram, principal, compileRunService, tempFolder);

      if (compileSuccess) {
        // set System.out to be a specific output stream in order to capture output of the
        // program and send it back to the user
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputStream);
        System.setOut(out);

        compileRunService.sendMessages(principal.getName(), "Compiled!");
        compileRunService.sendMessages(principal.getName(), "Running your program...");
        runClass(
            tempFolder.toURI().toURL(), userProgram.getFileName(), compileRunService, principal);

        // outputStream should now contain output of userProgram
        outputStream.flush();
        String result = outputStream.toString();
        if (result.length() > 0) {
          compileRunService.sendMessages(principal.getName(), result);
        }
      } else {
        compileRunService.sendMessages(
            principal.getName(), "There was an error compiling your program.");
      }

    } catch (IOException e) {
      // IOException could be called by creating a temporary folder or writing to that folder.
      // May need better error handling for this.
      compileRunService.sendMessages(
          principal.getName(), "There was an issue trying to run your program, please try again.");
      e.printStackTrace();
    }

    if (tempFolder != null) {
      tempFolder.delete();
    }

    // ensure System.out is reset
    System.setOut(System.out);
  }

  private static boolean compileProgram(
      UserProgram userProgram,
      Principal principal,
      CompileRunService compileRunService,
      File tempFolder) {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

    CompilationTask task = getCompilationTask(userProgram, tempFolder, diagnostics);
    if (task == null) {
      return false;
    }

    boolean success = task.call();

    // diagnostics will include any compiler errors
    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
      compileRunService.sendMessages(principal.getName(), diagnostic.toString());
    }
    return success;
  }

  // Given a user program, create a compilation task that will save the .class file to the given
  // temp folder and output any compilation messages to diagnostics.
  private static CompilationTask getCompilationTask(
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
        new JavaSourceFromString(userProgram.getFileName(), userProgram.getCode());
    Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);

    // create compilation task
    CompilationTask task =
        compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
    return task;
  }

  private static void runClass(
      URL filePath, String className, CompileRunService compileRunService, Principal principal) {
    URL[] classLoaderUrls = new URL[] {filePath};

    // Create a new URLClassLoader
    URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);

    try {
      // load and run the main method of the class
      urlClassLoader
          .loadClass(className)
          .getDeclaredMethod("main", new Class[] {String[].class})
          .invoke(null, new Object[] {null});

    } catch (ClassNotFoundException e) {
      // this should be caught earlier in compilation
      System.err.println("Class not found: " + e);
    } catch (NoSuchMethodException e) {
      compileRunService.sendMessages(
          principal.getName(), "Error: your program does not contain a main method");
    } catch (IllegalAccessException e) {
      // TODO: this error message may not be not very friendly
      compileRunService.sendMessages(principal.getName(), "Illegal access: " + e);
    } catch (InvocationTargetException e) {
      compileRunService.sendMessages(
          principal.getName(), "Your code hit an exception " + e.getCause().getClass().toString());
    }
    try {
      urlClassLoader.close();
    } catch (IOException e) {
      System.err.println("Error closing urlClassLoader: " + e);
    }
  }
}
