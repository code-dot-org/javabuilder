package org.code.javabuilder;

import java.io.PrintStream;
import org.code.protocol.OutputAdapter;

/**
 * Wrapper class for PrintStream to ensure autoFlush is set to true for PrintStreams that replace
 * System.out. autoFlush is the default for the System.in PrintStream.
 */
public class OutputPrintStream extends PrintStream {
  public OutputPrintStream(OutputAdapter adapter) {
    super(new OutputRedirectionStream(adapter), true);
  }
}
