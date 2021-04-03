package org.code.javabuilder;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

// TODO HERE
public class UserCodeCompiler {
  public void compileProgram() {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

    CompilationTask task = getCompilationTask("userProgram", null, diagnostics);
    if (task == null) {
//      return false;
    }

    boolean success = task.call();

    // diagnostics will include any compiler errors
    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
      System.out.println(diagnostic.toString());
    }
//    return success;
  }

  private CompilationTask getCompilationTask(
      String userProgram /*connect here*/, File tempFolder, DiagnosticCollector<JavaFileObject> diagnostics) {
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
    JavaFileObject file = new JavaSourceFromString("foo", "bar");
    Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);

    // create compilation task
    CompilationTask task =
        compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
    return task;
  }
}
