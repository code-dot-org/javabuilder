package org.code.javabuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
  private PipedInputStream systemInputStream;
  private PipedOutputStream systemInputWriter;

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
    } catch (IOException e) {
      this.compileRunService.sendMessages(
          principal.getName(),
          "We hit an error on our side while compiling your program. Try again.");
    }

    this.compileRunService.sendMessages(principal.getName(), "Compiling your program...");
    boolean compileSuccess = compileProgram(userProgram, principal, tempFolder);

    if (compileSuccess) {
      this.compileRunService.sendMessages(principal.getName(), "Compiled!");
      // We use a PipedInput and PipedOutput stream for I/O so we can control input to the user's
      // program and output from the user's program.
      // This try statement will close the streams automatically.
      try (PipedInputStream inputStream = new PipedInputStream();
          BufferedReader systemOutputReader =
              new BufferedReader(new InputStreamReader(inputStream));
          PipedOutputStream systemOutputStream = new PipedOutputStream(inputStream);
          PrintStream out = new PrintStream(systemOutputStream)) {
        // Set System.out to be a specific output stream in order to capture output of the
        // program and send it back to the user
        System.setOut(out);
        this.systemInputStream = new PipedInputStream();
        this.systemInputWriter = new PipedOutputStream(this.systemInputStream);
        System.setIn(this.systemInputStream);

        this.compileRunService.sendMessages(principal.getName(), "Running your program...");
        JavaExecutorThread userRuntime =
            new JavaExecutorThread(
                tempFolder.toURI().toURL(), userProgram, principal, this.compileRunService);
        userRuntime.start();

        while (userRuntime.isAlive() || systemOutputReader.ready()) {
          if (systemOutputReader.ready()) {
            // The user's program has produced output. Read it and pass it to the client console.
            this.compileRunService.sendMessages(principal.getName(), systemOutputReader.readLine());
          } else {
            // The user's program is running, but there's no output. Let it continue running.
            try {
              Thread.sleep(200);
            } catch (InterruptedException e) {
              this.compileRunService.sendMessages(
                  principal.getName(), "Your program ended unexpectedly. Try running it again.");
            }
            systemOutputStream.flush();
          }
        }
      } catch (IOException e) {
        this.compileRunService.sendMessages(
            principal.getName(), "There was an error processing your program's output.");
      }
    } else {
      this.compileRunService.sendMessages(
          principal.getName(), "There was an error compiling your program.");
    }

    if (tempFolder != null) {
      tempFolder.delete();
    }

    // ensure System.out is reset
    System.setOut(System.out);
    System.setIn(System.in);
  }

  /**
   * Write the given userInput to the user's (principal's) input stream
   *
   * @param userInput the given input directed to System.in
   * @param principal the user's identification
   */
  public void passInputToRuntime(UserInput userInput, Principal principal) {
    // System.in expects input to end with a lineSeparator. We add that to allow the user program to
    // continue.
    byte[] input = (userInput.getInput() + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
    try {
      this.systemInputWriter.write(input, 0, input.length);
      this.systemInputWriter.flush();
    } catch (IOException e) {
      this.compileRunService.sendMessages(
          principal.getName(), "There was an error passing input to your program.");
    }
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
}
