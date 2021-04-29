package dev.javabuilder;

import org.code.javabuilder.*;

/**
 * Intended for local testing only. This is a local version of the Javabuilder lambda function. The
 * LocalInputAdapter can be used to pass input to the program. The "MyClass.java" program in the
 * resources folder is the "user program." Output goes to the console.
 */
public class LocalMain {
  public static void main(String[] args) {
    final LocalInputAdapter inputAdapter = new LocalInputAdapter();
    final LocalOutputAdapter outputAdapter = new LocalOutputAdapter(System.out);
    final LocalProjectFileLoader fileLoader = new LocalProjectFileLoader();
    // Create and invoke the code execution environment
    try {
      UserProjectFiles userProjectFiles = fileLoader.loadFiles();
      try (CodeBuilder codeBuilder =
          new CodeBuilder(inputAdapter, outputAdapter, userProjectFiles)) {
        codeBuilder.buildUserCode();
        codeBuilder.runUserCode();
      }
    } catch (UserFacingException e) {
      outputAdapter.sendMessage(e.getMessage());
      outputAdapter.sendMessage("\n" + e.getLoggingString());
    } catch (UserInitiatedException e) {
      outputAdapter.sendMessage(e.getMessage());
      outputAdapter.sendMessage("\n" + e.getLoggingString());
    } catch (InternalFacingException e) {
      outputAdapter.sendMessage("\n" + e.getLoggingString());
    }
  }
}
