package org.code.protocol;

public interface ContentManager {
  byte[] getAssetData(String filename) throws JavabuilderException;

  String generateAssetUploadUrl(String filename) throws JavabuilderException;

  String writeToOutputFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException;
}
