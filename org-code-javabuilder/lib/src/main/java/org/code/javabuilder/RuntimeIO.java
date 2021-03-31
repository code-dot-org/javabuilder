package org.code.javabuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class RuntimeIO {
  private final PrintStream stdout;
  private final InputStream stdin;

  public RuntimeIO(OutputStream outputStream, InputStream inputStream) throws IOException {
    // Overwrite System.out
    this.stdout = System.out;
    System.setOut(new PrintStream(outputStream, true));

    // Overwrite System.in
    this.stdin = System.in;
    System.setIn(inputStream);
  }

  public void restoreSystemIO() {
    System.setOut(stdout);
    System.setIn(stdin);
  }
}
