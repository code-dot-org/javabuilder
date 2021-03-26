package org.code.lambda.javabuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RuntimeIO {
  private final PrintStream stdout;
  private final InputStream stdin;
  private final PipedOutputStream systemInputWriter;
  private final BufferedReader systemOutputReader;
  private final PipedOutputStream systemOutputStream;
  private final OutputSemaphore outputSemaphore;

  public RuntimeIO(OutputSemaphore outputSemaphore) throws IOException {
    this.outputSemaphore = outputSemaphore;

    // -- Redirect output from the user's program --
    PipedInputStream inputStream = new PipedInputStream();

    // Where can we read output from the user's program
    this.systemOutputReader = new BufferedReader(new InputStreamReader(inputStream));

    // The stream that collects output from the user's program. Flush this to read new output.
    this.systemOutputStream = new PipedOutputStream(inputStream);

    // Overwrite System.out
    this.stdout = System.out;
    System.setOut(new PrintStream(systemOutputStream));

    // -- Redirect input to the user's program --
    PipedInputStream systemInputStream = new PipedInputStream();

    // Where we can write input to the user's program
    this.systemInputWriter = new PipedOutputStream(systemInputStream);

    // Overwrite System.in
    this.stdin = System.in;
    System.setIn(systemInputStream);
  }

  public void passInputToProgram(String userInput) throws IOException {
    // System.in expects input to end with a lineSeparator. We add that to allow the user program to
    // continue.
    byte[] input = (userInput + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
    systemInputWriter.write(input, 0, input.length);
    systemInputWriter.flush();
  }

  public String pollForOutput() throws IOException {
    systemOutputStream.flush();
    if(systemOutputReader.ready()) {
      outputSemaphore.addOutputInProgress();
      return systemOutputReader.readLine();
    } else {
      outputSemaphore.decreaseOutputInProgress();
      return null;
    }
  }

  public void restoreSystemIO() {
    System.setOut(stdout);
    System.setIn(stdin);
  }
}
