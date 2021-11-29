package org.code.javabuilder;

import java.io.InputStream;
import org.code.protocol.JavabuilderException;

public interface ContentManager {
  InputStream getAssetInputStream(String filename);

  String generateAssetUploadUrl(String filename) throws JavabuilderException;

  String writeToAssetFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException;
}
