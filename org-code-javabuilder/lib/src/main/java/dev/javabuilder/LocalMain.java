package dev.javabuilder;

import org.code.javabuilder.*;

/**
 * Intended for local testing only. This is a local version of the Javabuilder lambda function. The
 * LocalInputAdapter can be used to pass input to the program. The "MyClass.java" program in the
 * resources folder is the "user program." Output goes to the console.
 */
public class LocalMain {
  public static void main(String[] args) throws Exception {
//    final LocalInputAdapter inputAdapter = new LocalInputAdapter();
//    final LocalOutputAdapter outputAdapter = new LocalOutputAdapter(System.out);
//    final LocalProjectFileManager fileManager = new LocalProjectFileManager();
//throw new Exception("ERROR!!!");
    System.out.println("In the main function.");

    String projectUrl = "http://localhost-studio.code.org:3000/v3/files/MoGwvaZmQSQZaImS3LliCQ";
    String[] fileNames = new String[]{"MyClass.java"};
    final UserProjectFileManager fileManager = new UserProjectFileManager(projectUrl, fileNames);
    final WebSocketInputAdapter inputAdapter = new WebSocketInputAdapter();
//    final WebSocketServer server = new WebSocketServer(inputAdapter);
    final WebSocketOutputAdapter outputAdapter = new WebSocketOutputAdapter();
    WebSocketConfig.setInputAdapter(inputAdapter);
    WebSocketConfig.setOutputAdapter(outputAdapter);
    System.out.println("In the main function.");


    // Create and invoke the code execution environment
    try (CodeBuilder codeBuilder = new CodeBuilder(inputAdapter, outputAdapter, fileManager)) {
      codeBuilder.compileUserCode();
      codeBuilder.runUserCode();
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
