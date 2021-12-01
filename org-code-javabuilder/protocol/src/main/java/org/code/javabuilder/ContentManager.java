package org.code.javabuilder;

import org.code.protocol.JavabuilderException;

public interface ContentManager {
  byte[] getAssetData(String filename) throws JavabuilderException;

  String generateAssetUploadUrl(String filename) throws JavabuilderException;

  String writeToOutputFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException;
}
