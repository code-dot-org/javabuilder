package dev.javabuilder;

import java.io.PrintStream;
import org.code.protocol.LogHandler;

public class LocalLogHandler implements LogHandler {
  private final PrintStream logStream;

  public LocalLogHandler(PrintStream logStream) {
    this.logStream = logStream;
  }

  @Override
  public void log(String log) {
    logStream.println(log);
  }
}
