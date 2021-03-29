package org.code.javabuilder;

import java.io.IOException;

public class OutputPoller extends Thread {
  private final JavaRunner javaRunner;
  private final OutputAdapter outputAdapter;
  private final RuntimeIO runtimeIO;
  private final OutputSemaphore outputSemaphore;
  public OutputPoller(JavaRunner javaRunner, OutputAdapter outputAdapter, RuntimeIO runtimeIO, OutputSemaphore outputSemaphore) {
    this.javaRunner = javaRunner;
    this.outputAdapter = outputAdapter;
    this.runtimeIO = runtimeIO;
    this.outputSemaphore = outputSemaphore;
  }

  public void run() {
    while (javaRunner.isAlive() || outputSemaphore.anyOutputInProgress()) {
      if(!javaRunner.isAlive()) {
        outputSemaphore.processFinalOutput();
      }
      String message = null;
      try {
        message = runtimeIO.pollForOutput();
      } catch (IOException e) {
        outputAdapter.sendMessage("There was an error reading output from your program. Try running it again." + e.toString());
      }
      if (message != null) {
        outputAdapter.sendMessage(message);
      }

      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        outputAdapter.sendMessage("There was an error reading output from your program. Try running it again." + e.toString());
      }
    }
  }
}
