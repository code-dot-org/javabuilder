package dev.javabuilder;

import org.code.protocol.JavabuilderUploadUrlGenerator;

public class LocalUrlGenerator implements JavabuilderUploadUrlGenerator {
  @Override
  public String getSignedUrl(String filename) {
    return "NOPE!";
  }

  @Override
  public String getFileUrl(String filename) {
    return "NOTTA CHANCE";
  }
}
