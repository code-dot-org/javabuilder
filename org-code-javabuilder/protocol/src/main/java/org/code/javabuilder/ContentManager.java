package org.code.javabuilder;

import org.code.protocol.JavabuilderException;

public interface ContentManager {
  String getAssetUrl(String filename);

  String getGeneratedInputFileUrl(String filename);

  String generateUploadUrl(String filename) throws JavabuilderException;

  String writeToFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException;
}
