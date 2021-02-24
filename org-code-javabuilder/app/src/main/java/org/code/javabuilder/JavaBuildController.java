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
    executeHelper(userProgram, principal);
    return new UserProgramOutput("Ran program!");
  }

  public void executeHelper(UserProgram userProgram, Principal principal) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    File tempFolder = null;
    try {
      tempFolder = Files.createTempDirectory("tmpdir").toFile();
      System.out.println(tempFolder.getAbsolutePath());
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(tempFolder));

      DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
      JavaFileObject file =
          new JavaSourceFromString(userProgram.getFileName(), userProgram.getCode());
      Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
      CompilationTask task =
          compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
      boolean success = task.call();

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(outputStream);
      System.setOut(out);

      if (success) {
        runClass(tempFolder.toURI().toURL(), userProgram.getFileName());
      }
      outputStream.flush();
      String result = outputStream.toString();
      compileRunService.sendMessages(principal.getName(), result);
    } catch (IOException e) {
      System.out.println(e.getStackTrace());
    }
    if (tempFolder != null) {
      tempFolder.delete();
    }
  }

  public void runClass(URL filePath, String className) {
    URL[] classLoaderUrls = new URL[] {filePath};

    // Create a new URLClassLoader
    URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);

    // Load the target class
    try {
      Class<?> hello = urlClassLoader.loadClass(className);
      hello
          .getDeclaredMethod("main", new Class[] {String[].class})
          .invoke(null, new Object[] {null});

    } catch (ClassNotFoundException e) {
      System.err.println("Class not found: " + e);
    } catch (NoSuchMethodException e) {
      System.err.println("No such method: " + e);
    } catch (IllegalAccessException e) {
      System.err.println("Illegal access: " + e);
    } catch (InvocationTargetException e) {
      System.err.println("Invocation target: " + e);
    }
    try {
      urlClassLoader.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
