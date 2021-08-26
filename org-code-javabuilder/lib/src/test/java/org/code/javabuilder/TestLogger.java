package org.code.javabuilder;

import org.code.protocol.LogHandler;

public class TestLogger implements LogHandler {
  @Override
  public void log(String log) {
    System.out.println(log);
  }
}
