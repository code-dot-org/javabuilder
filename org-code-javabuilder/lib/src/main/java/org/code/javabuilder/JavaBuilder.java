package org.code.javabuilder;

import java.io.PrintStream;

public class JavaBuilder {
  private final OutputAdapter outputAdapter;
  private final InputAdapter inputAdapter;

  public JavaBuilder(InputAdapter inputAdapter, OutputAdapter outputAdapter) {
    this.outputAdapter = outputAdapter;
    this.inputAdapter = inputAdapter;
  }

  public void runUserCode() {
    System.setOut(new PrintStream(new OutputRedirectionStream(this.outputAdapter), true));
    System.setIn(new InputRedirectionStream(this.inputAdapter));
    JavaRunner runner = new JavaRunner();
    runner.runCode();
  }
}
