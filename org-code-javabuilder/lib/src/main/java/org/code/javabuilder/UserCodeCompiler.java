package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.logging.Logger;
import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import org.code.javabuilder.util.JarUtils;
import org.code.protocol.*;

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
   * @throws InternalServerError If the user's code has a compiler error or if we hit an internal
   *     exception that interferes with compilation.
   */
  public void compileProgram() throws InternalServerError, UserInitiatedException {
    this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.COMPILING));
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    PerformanceTracker.getInstance().trackCompileStart();
    CompilationTask task = getCompilationTask(diagnostics);

    boolean success = task.call();
    PerformanceTracker.getInstance().trackCompileEnd();

    // diagnostics will include any compiler errors
    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
      String customMessage = this.getCustomCompilerError(diagnostic);
      if (customMessage != null) {
        // If we got a custom message, just send it and stop sending any more diagnostics to avoid
        // confusion.
        outputAdapter.sendMessage(new SystemOutMessage(customMessage));
        break;
      }
      outputAdapter.sendMessage(new SystemOutMessage(this.getCompilerError(diagnostic)));
    }
    if (!success) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.COMPILER_ERROR);
    }
    this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.COMPILATION_SUCCESSFUL));
  }

  private CompilationTask getCompilationTask(DiagnosticCollector<JavaFileObject> diagnostics)
      throws InternalServerError, UserInitiatedException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    // set output of compilation to be a temporary folder
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    try {
      fileManager.setLocation(
          StandardLocation.CLASS_OUTPUT, Collections.singletonList(this.tempFolder));
    } catch (IOException e) {
      e.printStackTrace();
      // if we can't set the file location we won't be able to run the class properly.
      throw new InternalServerError(InternalErrorKey.INTERNAL_COMPILER_EXCEPTION, e);
    }
    // create file for user-provided code
    List<JavaFileObject> files = new ArrayList<>();
    for (JavaProjectFile projectFile : this.javaFiles) {
      try {
        files.add(
            new JavaSourceFromString(projectFile.getClassName(), projectFile.getFileContents()));
      } catch (IllegalArgumentException e) {
        // Thrown if the project file name is invalid. Wrap the original filename in an exception so
        // it can be surfaced to the user
        throw new UserInitiatedException(
            UserInitiatedExceptionKey.INVALID_JAVA_FILE_NAME,
            new Exception(projectFile.getFileName()));
      }
    }

    // Include the user-facing api jars in the student code classpath so the student code can use
    // them.
    List<String> optionList = new ArrayList<String>();
    optionList.add("-classpath");
    optionList.add(JarUtils.getAllJarPaths());

    // create compilation task
    return compiler.getTask(null, fileManager, diagnostics, optionList, null, files);
  }

  /**
   * For specific compiler errors we want to return custom messages. Check if the given diagnostic
   * is due to one of those errors and return the more specific message, or null if not.
   *
   * @param diagnostic
   * @return custom compiler error or null
   */
  private String getCustomCompilerError(Diagnostic<? extends JavaFileObject> diagnostic) {
    // Check if the compiler error is due to the student importing java.lang.System
    // directly and give a more helpful message.
    if (diagnostic.getCode().equals("compiler.err.already.defined.single.import")
        && diagnostic.getMessage(Locale.US).contains("org.code.lang.System")) {
      return "Import of java.lang.System is not supported.";
    }
    return null;
  }

  /**
   * Get the compiler error as a String from the given diagnostic. We create a compiler error here
   * because we need to subtract 1 from the line number due to our auto-import of System.
   *
   * <p>The compiler error format is:
   *
   * <pre>
   * FileName:line number: error/warning: {error type}
   * {code snippet}
   * ^ (caret pointing to location of error)
   * {error details if applicable}
   * </pre>
   *
   * <p>Example:
   *
   * <pre>
   * /SystemTest.java:26: error: cannot find symbol
   *     System.out.println(System.currenTimeMillis());
   *                              ^
   *   symbol:   method currenTimeMillis()
   *   location: class org.code.lang.System
   * </pre>
   *
   * @param diagnostic
   * @return compiler error String
   */
  private String getCompilerError(Diagnostic<? extends JavaFileObject> diagnostic) {
    if (diagnostic.getSource() == null || diagnostic.getLineNumber() == Diagnostic.NOPOS) {
      Logger.getLogger(MAIN_LOGGER)
          .warning(
              "Falling back to default compiler error, diagnostic source was null or line number was -1. Diagnostic error code is "
                  + diagnostic.getCode());
      return diagnostic.toString();
    }
    // Subtract 1 from the line number to account for our auto-import
    long lineNumber = diagnostic.getLineNumber() - 1;
    String diagnosticMessage = diagnostic.getMessage(Locale.US);
    String firstMessageLine = diagnosticMessage;
    String secondMessageLine = "";
    // If the diagnostic message has multiple lines, split it into two strings. We will
    // put the code snippet after the first line, then put the details after the code snippet.
    if (diagnosticMessage.indexOf("\n") > 0) {
      firstMessageLine = diagnosticMessage.substring(0, diagnosticMessage.indexOf("\n"));
      secondMessageLine = diagnosticMessage.substring(diagnosticMessage.indexOf("\n") + 1) + "\n";
    }
    String message =
        String.format(
            "%s:%d: %s: %s",
            diagnostic.getSource().getName(),
            lineNumber,
            diagnostic.getKind().toString().toLowerCase(),
            firstMessageLine);
    String codeSnippet = this.getCodeSnippet(diagnostic);
    return String.format("%s\n%s\n%s", message, codeSnippet, secondMessageLine);
  }

  /**
   * Get the code from the line where the diagnostic error occurred, as well as a pointer to the
   * specific location of the error. For example:
   *
   * <pre>
   *     System.out.println(System.currenTimeMillis());
   *                              ^
   * </pre>
   *
   * @param diagnostic
   * @return code snippet as a String, or an empty String if a code snippet could not be created.
   */
  private String getCodeSnippet(Diagnostic<? extends JavaFileObject> diagnostic) {
    String codeSnippet = "";
    try {
      Reader reader = diagnostic.getSource().openReader(false);
      Scanner scanner = new Scanner(reader);
      int linesRead = 0;
      while (linesRead < diagnostic.getLineNumber() - 1) {
        scanner.nextLine();
        linesRead++;
      }
      codeSnippet = scanner.nextLine();
    } catch (IOException e) {
      // If we had an issue reading the code, log the error and return an empty String
      // so we still can get a somewhat useful compiler error.
      LoggerUtils.logException(e);
      return "";
    }
    String linePointer = "";
    for (int i = 1; i < diagnostic.getColumnNumber(); i++) {
      linePointer += " ";
    }
    linePointer += "^";
    return String.format("%s\n%s", codeSnippet, linePointer);
  }
}
