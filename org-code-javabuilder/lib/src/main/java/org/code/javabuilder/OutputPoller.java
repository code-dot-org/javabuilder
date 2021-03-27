package org.code.javabuilder;

import java.io.IOException;

public class OutputPoller extends Thread {
  private final JavaRunner javaRunner;
  private final OutputHandler outputHandler;
  private final RuntimeIO runtimeIO;
  private final OutputSemaphore outputSemaphore;

  public OutputPoller(
      JavaRunner javaRunner,
      OutputHandler outputHandler,
      RuntimeIO runtimeIO,
      OutputSemaphore outputSemaphore) {
    this.javaRunner = javaRunner;
    this.outputHandler = outputHandler;
    this.runtimeIO = runtimeIO;
    this.outputSemaphore = outputSemaphore;
  }

  public void run() {
    while (javaRunner.isAlive() || outputSemaphore.anyOutputInProgress()) {
      if (!javaRunner.isAlive()) {
        outputSemaphore.processFinalOutput();
      }
      String message = null;
      try {
        message = runtimeIO.pollForOutput();
      } catch (IOException e) {
        outputHandler.sendMessage(
            "There was an error reading output from your program. Try running it again."
                + e.toString());
      }
      if (message != null) {
        outputHandler.sendMessage(message);
      }

      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        outputHandler.sendMessage(
            "There was an error reading output from your program. Try running it again."
                + e.toString());
      }
    }
  }
}
