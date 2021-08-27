package dev.javabuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.code.javabuilder.*;
import org.code.javabuilder.InternalError;
import org.code.protocol.InternalErrorKey;

/** Intended for local testing only. Loads the main.json file from the resources folder. */
public class LocalProjectFileLoader implements ProjectFileLoader {

  @Override
  public UserProjectFiles loadFiles() throws InternalError, UserInitiatedException {
    try {
      String mainJson =
          Files.readString(
              Paths.get(getClass().getClassLoader().getResource("main_painter.json").toURI()));
      return new UserProjectFileParser().parseFileJson(mainJson);
    } catch (IOException | URISyntaxException e) {
      throw new InternalError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }
}
