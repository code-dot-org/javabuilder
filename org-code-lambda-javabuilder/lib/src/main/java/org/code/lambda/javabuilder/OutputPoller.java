package org.code.lambda.javabuilder;

import java.io.IOException;

public class OutputPoller extends Thread {
  private final OutputHandler outputHandler;
  public OutputPoller(OutputHandler outputHandler) {
    this.outputHandler = outputHandler;
  }

  public void run() {
    int x = 0;
    while (x < 100) {
      x++;
      String message = null;
      try {
        message = RuntimeIO.pollForOutput();
      } catch (IOException e) {
        outputHandler.sendMessage("There was an error reading output from your program. Try running it again." + e.toString());
      }
      if (message != null) {
        outputHandler.sendMessage(message);
      }

      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        outputHandler.sendMessage("There was an error reading output from your program. Try running it again." + e.toString());
      }
    }
  }
}
