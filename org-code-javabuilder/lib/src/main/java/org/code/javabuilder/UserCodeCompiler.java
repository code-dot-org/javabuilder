package org.code.javabuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

/**
 * Compiles all user code managed by the ProjectFileManager. Any compiler output will be passed
 * directly to the user.
 */
public class UserCodeCompiler {
  private final List<JavaProjectFile> javaFiles;
  private final File tempFolder;
  private final OutputAdapter outputAdapter;

  public UserCodeCompiler(
      List<JavaProjectFile> javaFiles, File tempFolder, OutputAdapter outputAdapter) {
    this.javaFiles = javaFiles;
    this.tempFolder = tempFolder;
    this.outputAdapter = outputAdapter;
  }

  /**
   * @throws UserFacingException If the user's code has a compiler error or if we hit an internal
   *     exception that interferes with compilation.
   */
  public void compileProgram() throws UserFacingException, UserInitiatedException {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    CompilationTask task = getCompilationTask(diagnostics);

    boolean success = task.call();

    // diagnostics will include any compiler errors
    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
      outputAdapter.sendMessage(new SystemOutMessage(diagnostic.toString()));
    }
    if (!success) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.compilerError);
    }
  }

  private CompilationTask getCompilationTask(DiagnosticCollector<JavaFileObject> diagnostics)
      throws UserFacingException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    // set output of compilation to be a temporary folder
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    try {
      fileManager.setLocation(
          StandardLocation.CLASS_OUTPUT, Collections.singletonList(this.tempFolder));
    } catch (IOException e) {
      e.printStackTrace();
      // if we can't set the file location we won't be able to run the class properly.
      throw new UserFacingException(UserFacingExceptionKey.internalCompilerException, e);
    }
    // create file for user-provided code
    List<JavaFileObject> files = new ArrayList<>();
    for (JavaProjectFile projectFile : this.javaFiles) {
      files.add(
          new JavaSourceFromString(projectFile.getClassName(), projectFile.getFileContents()));
    }

    // create compilation task
    return compiler.getTask(null, fileManager, diagnostics, null, null, files);
  }
}
