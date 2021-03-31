package org.code.javabuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RuntimeIO {
  private final PrintStream stdout;
  private final InputStream stdin;
  private PipedOutputStream systemInputWriter;

  public RuntimeIO(OutputStream outputStream) throws IOException {
    // Overwrite System.out
    this.stdout = System.out;
    System.setOut(new PrintStream(outputStream, true));

    // -- Redirect input to the user's program --
    PipedInputStream systemInputStream = new PipedInputStream();

    // Where we can write input to the user's program
    this.systemInputWriter = new PipedOutputStream(systemInputStream);

    // Overwrite System.in
    this.stdin = System.in;
    System.setIn(systemInputStream);
  }

  public RuntimeIO(OutputStream outputStream, InputStream inputStream) throws IOException {
    // Overwrite System.out
    this.stdout = System.out;
    System.setOut(new PrintStream(outputStream, true));

    // Overwrite System.in
    this.stdin = System.in;
    System.setIn(inputStream);
  }

  public void passInputToProgram(String userInput) throws IOException {
    // System.in expects input to end with a lineSeparator. We add that to allow the user program to
    // continue.
    byte[] input = (userInput + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
    systemInputWriter.write(input, 0, input.length);
    systemInputWriter.flush();
  }

  public void restoreSystemIO() {
    System.setOut(stdout);
    System.setIn(stdin);
  }
}
