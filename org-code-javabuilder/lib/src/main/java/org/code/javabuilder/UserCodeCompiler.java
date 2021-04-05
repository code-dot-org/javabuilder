package org.code.javabuilder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

/**
 * Compiles all user code managed by the ProjectFileManager. Any compiler output will be passed
 * directly to the user.
 */
public class UserCodeCompiler {
  private final ProjectFileManager projectFileManager;
  private final File tempFolder;
  private final OutputAdapter outputAdapter;

  public UserCodeCompiler(
      ProjectFileManager projectFileManager, File tempFolder, OutputAdapter outputAdapter) {
    this.projectFileManager = projectFileManager;
    this.tempFolder = tempFolder;
    this.outputAdapter = outputAdapter;
  }

  /**
   * @throws UserFacingException If the user's code has a compiler error or if we hit an internal
   *     exception that interferes with compilation.
   */
  public void compileProgram() throws UserFacingException {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    CompilationTask task = getCompilationTask(diagnostics);

    boolean success = task.call();

    // diagnostics will include any compiler errors
    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
      outputAdapter.sendMessage(diagnostic.toString());
    }
    if (!success) {
      throw new UserFacingException(
          "We couldn't compile your program. Look for bugs in your program and try again.");
    }
  }

  private CompilationTask getCompilationTask(DiagnosticCollector<JavaFileObject> diagnostics)
      throws UserFacingException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    // set output of compilation to be a temporary folder
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    try {
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(tempFolder));
    } catch (IOException e) {
      e.printStackTrace();
      // if we can't set the file location we won't be able to run the class properly.
      throw new UserFacingException(
          "We hit an error on our side while compiling your program. Try again.");
    }

    // create file for user-provided code
    ProjectFile projectFile = projectFileManager.getFile();
    JavaFileObject file =
        new JavaSourceFromString(projectFile.getClassName(), projectFile.getCode());
    Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);

    // create compilation task
    CompilationTask task =
        compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
    return task;
  }
}