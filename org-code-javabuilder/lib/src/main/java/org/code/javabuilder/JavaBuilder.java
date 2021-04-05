package org.code.javabuilder;

/** The orchestrator for code compilation and execution. */
public class JavaBuilder {
  private final OutputAdapter outputAdapter;
  private final InputAdapter inputAdapter;

  public JavaBuilder(InputAdapter inputAdapter, OutputAdapter outputAdapter) {
    this.outputAdapter = outputAdapter;
    this.inputAdapter = inputAdapter;
  }

  /**
   * Sets replaces System.in and System.out with our custom implementation and executes the user's
   * code.
   */
  public void runUserCode() {
    System.setOut(new OutputPrintStream(this.outputAdapter));
    System.setIn(new InputRedirectionStream(this.inputAdapter));
    JavaRunner runner = new JavaRunner();
    runner.runCode();
  }
}
