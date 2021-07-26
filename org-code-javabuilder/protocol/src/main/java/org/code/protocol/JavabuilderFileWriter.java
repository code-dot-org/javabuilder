package org.code.protocol;

public interface JavabuilderFileWriter {
  String writeToFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException;
}
