package dev.javabuilder;

import org.code.protocol.JavabuilderException;
import org.code.protocol.JavabuilderFileWriter;

public class NoOpFileWriter implements JavabuilderFileWriter {
  @Override
  public String writeToFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException {
    return "";
  }
}
