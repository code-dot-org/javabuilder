package org.code.javabuilder;

import java.io.IOException;

public class JavaBuilder {
  private final OutputAdapter outputAdapter;
  private final InputAdapter inputAdapter;
  private final JavaRunner javaRunner;

  public JavaBuilder(InputAdapter inputAdapter, OutputAdapter outputAdapter) {
    this.outputAdapter = outputAdapter;
    this.inputAdapter = inputAdapter;

    // Overwrite system I/O
    RuntimeIO runtimeIO;
    OutputRedirectionStream outputRedirectionStream = new OutputRedirectionStream(this.outputAdapter);
    InputRedirectionStream inputRedirectionStream = new InputRedirectionStream(this.inputAdapter);
    this.outputAdapter.sendMessage("Update 5");

    try {
      runtimeIO = new RuntimeIO(outputRedirectionStream, inputRedirectionStream);
    } catch (IOException e) {
      this.outputAdapter.sendMessage("There was an error running your code. Try again.");
      throw new RuntimeException("Error setting up console IO", e);
    }

    // Create code runner
    this.javaRunner = new JavaRunner();
  }

  public void runUserCode() {
    this.javaRunner.start();
    while(javaRunner.isAlive()) {
      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        outputAdapter.sendMessage("There was an error running to your program. Try running it again." + e.toString());
      }
    }
  }
}
