package org.code.protocol;

/**
 * Manages content (such as code sources and assets) that are used and generated during a
 * Javabuilder session.
 */
public interface ContentManager {
  byte[] getAssetData(String filename) throws JavabuilderException;

  String generateAssetUploadUrl(String filename) throws JavabuilderException;

  String writeToOutputFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException;
}
