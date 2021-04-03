package org.code.javabuilder;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProjectFileManager {
  private final String baseUrl;
  private final String[] fileNames;
  private final ArrayList<ProjectFile> fileList;

  public ProjectFileManager(String baseUrl, String[] fileNames) {
    this.baseUrl = baseUrl;
    this.fileNames = fileNames;
    this.fileList = new ArrayList<>();
  }

  // GOOD
  public void loadFiles() throws InterruptedException, Exception {
    ExecutorService executor = Executors.newCachedThreadPool();
    for(String fileName : fileNames) {
      ProjectFile file = new ProjectFile(fileName);
      fileList.add(file);
      executor.execute(new ProjectLoader(file, String.join(baseUrl, fileName, "/")));
    }
    executor.shutdown();
    boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);
    if (!finished) {
      // TODO: Fix this exception
      throw new Exception("Could not load your files");
    }
  }
}
