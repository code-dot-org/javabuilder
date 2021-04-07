package org.code.javabuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/** Manages the in-memory list of user project files. */
public class UserProjectFileManager implements ProjectFileManager {
  private final String baseUrl;
  private final String[] fileNames;
  private final ArrayList<ProjectFile> fileList;

  public UserProjectFileManager(String baseUrl, String[] fileNames) {
    this.baseUrl = baseUrl;
    this.fileNames = fileNames;
    this.fileList = new ArrayList<>();
  }

  /**
   * Asynchronously loads all user project files.
   *
   * @throws UserFacingException If the load times out or fails.
   */
  public void loadFiles() throws UserFacingException {
    ExecutorService executor = Executors.newCachedThreadPool();
    List<Callable<Boolean>> callables = new ArrayList<>();
    for (String fileName : fileNames) {
      ProjectFile file = new ProjectFile(fileName);
      fileList.add(file);
      callables.add(new ProjectFileLoader(file, String.join("/", baseUrl, fileName)));
    }
    List<Future<Boolean>> futures;
    try {
      futures = executor.invokeAll(callables);
      executor.shutdown();
      boolean finished;
      finished = executor.awaitTermination(10, TimeUnit.SECONDS);

      if (!finished) {
        throw new UserFacingException(
            "We couldn't fetch your files before our loader timed out. Try again.");
      }
      for (Future<Boolean> future : futures) {
        if (!future.get()) {
          throw new UserFacingException(
              "We hit an error on our side while loading your files. Try again.");
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new UserFacingException(
          "We hit an error on our side while loading your files. Try again.", e);
    }
  }

  /**
   * Returns the user project file. Currently, we only allow a single user project file. TODO:
   * Enable multi-file programs
   *
   * @return the user project file.
   */
  @Override
  public ProjectFile getFile() {
    return fileList.get(0);
  }
}
