package org.code.protocol;

public class TestLogHandler implements LogHandler {
  @Override
  public void log(String log) {
    System.out.println(log);
  }
}
