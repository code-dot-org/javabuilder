package org.code.javabuilder;

import java.io.IOException;

public class JavaBuilder {
  private final OutputAdapter outputAdapter;
  private final InputAdapter inputAdapter;
  private final InputPoller inputPoller;
//  private final OutputPoller outputPoller;
  private final JavaRunner javaRunner;
  private final OutputSemaphore outputSemaphore;

  public JavaBuilder(InputAdapter inputAdapter, OutputAdapter outputAdapter) {
    this.outputAdapter = outputAdapter;
    this.inputAdapter = inputAdapter;
    this.outputSemaphore = new OutputSemaphore();

    // Overwrite system I/O
    RuntimeIO runtimeIO;
//    PrintStream printStream = new PrintStream();
    CustomOutputStream customOutputStream = new CustomOutputStream(System.out, this.outputAdapter);
    OutputRedirectionStream outputRedirectionStream = new OutputRedirectionStream(this.outputAdapter);

    try {
      runtimeIO = new RuntimeIO(outputRedirectionStream);//this.outputSemaphore);
    } catch (IOException e) {
      this.outputAdapter.sendMessage("There was an error running your code. Try again.");
      throw new RuntimeException("Error setting up console IO", e);
    }

    // Create code runner
    this.javaRunner = new JavaRunner(this.outputSemaphore);

    // Create input poller
    this.inputPoller = new InputPoller(this.inputAdapter, runtimeIO, this.javaRunner, this.outputAdapter);

    // Create output poller
//    this.outputPoller = new OutputPoller(this.javaRunner, this.outputAdapter, runtimeIO, this.outputSemaphore);
  }

  public void runUserCode() {
    this.javaRunner.start();
    this.inputPoller.start();
//    this.outputPoller.start();
    while(javaRunner.isAlive()) {
      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        outputAdapter.sendMessage("There was an error running to your program. Try running it again." + e.toString());
      }
    }

    while (outputSemaphore.anyOutputInProgress()) {
      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        outputAdapter.sendMessage("There was an error running to your program. Try running it again." + e.toString());
      }
    }
  }
}
