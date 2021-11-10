package org.code.protocol;

public interface JavabuilderUploadUrlGenerator {
  String getSignedUrl(String filename);

  String getFileUrl(String filename);
}
