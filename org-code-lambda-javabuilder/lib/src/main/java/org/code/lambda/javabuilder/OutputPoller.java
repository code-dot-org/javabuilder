package org.code.lambda.javabuilder;

import java.io.IOException;

public class OutputPoller extends Thread {
  private final JavaRunner javaRunner;
  public OutputPoller(JavaRunner javaRunner) {
    this.javaRunner = javaRunner;
  }

  public void run() {
    while (javaRunner.isAlive() || OutputSemaphore.anyOutputInProgress()) {
      if(!javaRunner.isAlive()) {
        OutputHandler.sendDebuggingMessage("Processing Final Output");
        OutputSemaphore.processFinalOutput();
        OutputHandler.sendDebuggingMessage("Should be false: " + String.valueOf(javaRunner.isAlive() || OutputSemaphore.anyOutputInProgress()));
      }
      String message = null;
      try {
        message = RuntimeIO.pollForOutput();
      } catch (IOException e) {
        OutputHandler.sendMessage("There was an error reading output from your program. Try running it again." + e.toString());
      }
      if (message != null) {
        OutputHandler.sendMessage(message);
      }

      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        OutputHandler.sendMessage("There was an error reading output from your program. Try running it again." + e.toString());
      }
    }

    OutputHandler.sendDebuggingMessage("Done polling for output");
  }
}
