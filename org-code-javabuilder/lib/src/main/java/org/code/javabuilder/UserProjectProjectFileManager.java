package org.code.javabuilder;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UserProjectProjectFileManager implements ProjectFileManager {
  private final String baseUrl;
  private final String[] fileNames;
  private final ArrayList<ProjectFile> fileList;

  public UserProjectProjectFileManager(String baseUrl, String[] fileNames) {
    this.baseUrl = baseUrl;
    this.fileNames = fileNames;
    this.fileList = new ArrayList<>();
  }

  public void loadFiles() throws UserFacingException {
    ExecutorService executor = Executors.newCachedThreadPool();
    for (String fileName : fileNames) {
      ProjectFile file = new ProjectFile(fileName);
      fileList.add(file);
      executor.execute(new ProjectLoader(file, String.join(baseUrl, fileName, "/")));
    }
    executor.shutdown();
    boolean finished;
    try {
      finished = executor.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new UserFacingException(
          "We hit an error on our side while loading your files. Try again.");
    }
    if (!finished) {
      throw new UserFacingException(
          "We couldn't fetch your files before our loader timed out. Try again.");
    }
  }

  @Override
  public ProjectFile getFile() {
    return fileList.get(0);
  }
}
