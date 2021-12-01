package dev.javabuilder;

import static dev.javabuilder.LocalWebserverConstants.DIRECTORY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.code.javabuilder.*;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.JavabuilderException;

public class LocalContentManager implements ContentManager, ProjectFileLoader {
  private static final String SERVER_URL_FORMAT = "http://localhost:8080/%s/%s/%s";
  private static final String SOURCES_SUBDIRECTORY = "sources";
  private static final String ASSETS_SUBDIRECTORY = "assets";
  private static final String OUTPUT_SUBDIRECTORY = "output";

  private final String javabuilderSessionId;
  private final UserProjectFileParser projectFileParser;

  public LocalContentManager(String javabuilderSessionId) {
    this(javabuilderSessionId, new UserProjectFileParser());
  }

  LocalContentManager(String javabuilderSessionId, UserProjectFileParser projectFileParser) {
    this.javabuilderSessionId = javabuilderSessionId;
    this.projectFileParser = projectFileParser;
  }

  @Override
  public UserProjectFiles loadFiles() throws InternalServerError, UserInitiatedException {
    final File mainJson = this.getLocalFile(SOURCES_SUBDIRECTORY, "main.json");
    try {
      System.out.println(Files.readString(mainJson.toPath()));
      final UserProjectFiles projectFiles =
          this.projectFileParser.parseFileJson(Files.readString(mainJson.toPath()));
      final File grid = this.getLocalFile(SOURCES_SUBDIRECTORY, "grid.txt");
      if (grid.exists()) {
        projectFiles.addTextFile(new TextProjectFile("grid.txt", Files.readString(grid.toPath())));
      }
      return projectFiles;
    } catch (IOException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
  }

  @Override
  public byte[] getAssetData(String filename) throws JavabuilderException {
    final File assetFile = this.getLocalFile(ASSETS_SUBDIRECTORY, filename);
    try {
      return Files.readAllBytes(assetFile.toPath());
    } catch (IOException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
  }

  @Override
  public String generateAssetUploadUrl(String filename) throws JavabuilderException {
    File parentDirectory = Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY).toFile();
    if (!parentDirectory.exists()) {
      parentDirectory.mkdirs();
    }
    return this.getPath(ASSETS_SUBDIRECTORY, filename);
  }

  @Override
  public String writeToOutputFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException {
    File file = this.getLocalFile(OUTPUT_SUBDIRECTORY, filename);
    try {
      File parentDirectory = this.getLocalFile(OUTPUT_SUBDIRECTORY, "");
      if (!parentDirectory.exists()) {
        parentDirectory.mkdirs();
      }
      Files.write(file.toPath(), inputBytes);
    } catch (IOException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
    return this.getPath(OUTPUT_SUBDIRECTORY, filename);
  }

  private String getPath(String subdirectory, String filename) {
    return String.format(SERVER_URL_FORMAT, DIRECTORY, subdirectory, filename);
  }

  private File getLocalFile(String subdirectory, String filename) {
    return Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY, subdirectory, filename)
        .toFile();
  }
}
