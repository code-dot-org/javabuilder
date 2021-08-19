package dev.javabuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.code.javabuilder.UserFacingException;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.JavabuilderException;
import org.code.protocol.JavabuilderFileWriter;

public class LocalFileWriter implements JavabuilderFileWriter {
  @Override
  public String writeToFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException {
    File file = new File(filename);
    try {
      Files.write(file.toPath(), inputBytes);
    } catch (IOException e) {
      throw new UserFacingException(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
    return "http://localhost:8080/files/" + filename;
  }
}
