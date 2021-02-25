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
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

/**
 * Accepts requests to the /execute channel to compile and run user code. Directs output from the
 * user's program to the user on the /topic/output channel.
 */
@Controller
public class JavaBuildController {

  private final CompileRunService compileRunService;

  JavaBuildController(CompileRunService compileRunService) {
    this.compileRunService = compileRunService;
  }

  /** Executes the user code and sends the output of that code across the established websocket. */
  @MessageMapping(Destinations.EXECUTE_CODE)
  @SendToUser(Destinations.PTP_PREFIX + Destinations.OUTPUT_CHANNEL)
  public UserProgramOutput execute(UserProgram userProgram, Principal principal) throws Exception {
    // TODO: CSA-48 Handle more than one file
    String filename = userProgram.getFileName();
    // We expect the filename to have no .java suffix, remove it if necessary.
    if (filename.endsWith(".java")) {
      userProgram.setFileName(filename.substring(0, filename.indexOf(".java")));
    }
    executeHelper(userProgram, principal);
    return new UserProgramOutput("Done!");
  }

  public void executeHelper(UserProgram userProgram, Principal principal) {
    File tempFolder = null;
    try {
      tempFolder = Files.createTempDirectory("tmpdir").toFile();
      DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

      CompilationTask task = getCompilationTask(userProgram, tempFolder, diagnostics);
      compileRunService.sendMessages(principal.getName(), "Compiling your program...");
      boolean success = task.call();

      // diagnostics will include any compiler errors
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
        compileRunService.sendMessages(principal.getName(), diagnostic.toString());
      }

      if (success) {
        // set System.out to be a specific output stream in order to capture output of the
        // program and send it back to the user
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputStream);
        System.setOut(out);

        compileRunService.sendMessages(principal.getName(), "Compiled!");
        compileRunService.sendMessages(principal.getName(), "Running your program...");
        runClass(tempFolder.toURI().toURL(), userProgram.getFileName(), principal);

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
      System.out.println(e.getStackTrace());
    }

    if (tempFolder != null) {
      tempFolder.delete();
    }

    // ensure System.out is reset
    System.setOut(System.out);
  }

  // Given a user program, create a compilation task that will save the .class file to the given
  // temp folder and output any compilation messages to diagnostics.
  public CompilationTask getCompilationTask(
      UserProgram userProgram, File tempFolder, DiagnosticCollector<JavaFileObject> diagnostics)
      throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    // set output of compilation to be a temporary folder
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(tempFolder));

    // create file for user-provided code
    JavaFileObject file =
        new JavaSourceFromString(userProgram.getFileName(), userProgram.getCode());
    Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);

    // create compilation task
    CompilationTask task =
        compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
    return task;
  }

  public void runClass(URL filePath, String className, Principal principal) {
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
