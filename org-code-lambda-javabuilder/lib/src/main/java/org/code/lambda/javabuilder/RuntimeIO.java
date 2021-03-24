package org.code.lambda.javabuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RuntimeIO {
  private static PrintStream stdout;
  private static InputStream stdin;
  private static PipedOutputStream systemInputWriter;
  private static BufferedReader systemOutputReader;
  private static PipedOutputStream systemOutputStream;
//  private static RuntimeIO RUNTIME_IO_INSTANCE = null;


  public RuntimeIO() throws IOException {
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

  public static void passInputToProgram(String userInput) throws IOException {
    // System.in expects input to end with a lineSeparator. We add that to allow the user program to
    // continue.
    byte[] input = (userInput + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
    RuntimeIO.systemInputWriter.write(input, 0, input.length);
    RuntimeIO.systemInputWriter.flush();
  }

  public static String pollForOutput() throws IOException {
    RuntimeIO.systemOutputStream.flush();
    if(RuntimeIO.systemOutputReader.ready()) {
      OutputSemaphore.addOutputInProgress();
      OutputSemaphore.processFinalOutput();
      return RuntimeIO.systemOutputReader.readLine();
    } else {
      OutputSemaphore.processFinalOutput();
      return null;
    }
  }

  public static boolean areStreamsEmpty() throws IOException {
    return RuntimeIO.systemOutputReader.ready();
  }

  public static void restoreSystemIO() {
    System.setOut(RuntimeIO.stdout);
    System.setIn(RuntimeIO.stdin);
  }
}
