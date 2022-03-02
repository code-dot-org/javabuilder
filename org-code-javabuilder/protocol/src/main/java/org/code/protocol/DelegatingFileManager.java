package org.code.protocol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A {@link JavabuilderFileManager} that delegates to either an implementation configured to work
 * with Dashboard or to a {@link ContentManager}. Used to temporarily support two code paths.
 */
public class DelegatingFileManager implements JavabuilderFileManager {

  private final JavabuilderFileManager dashboardFileManager;
  private final ContentManager contentManager;
  private final boolean useDashboardSources;

  public DelegatingFileManager(
      JavabuilderFileManager dashboardFileManager,
      ContentManager contentManager,
      boolean useDashboardSources) {
    this.dashboardFileManager = dashboardFileManager;
    this.contentManager = contentManager;
    this.useDashboardSources = useDashboardSources;
  }

  @Override
  public String writeToFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException {
    return this.useDashboardSources
        ? this.dashboardFileManager.writeToFile(filename, inputBytes, contentType)
        : this.contentManager.writeToOutputFile(filename, inputBytes, contentType);
  }

  @Override
  public String getUploadUrl(String filename) throws JavabuilderException {
    return this.useDashboardSources
        ? this.dashboardFileManager.getUploadUrl(filename)
        : this.contentManager.generateAssetUploadUrl(filename);
  }

  @Override
  public URL getFileUrl(String filename) throws FileNotFoundException {
    if (this.useDashboardSources) {
      return this.dashboardFileManager.getFileUrl(filename);
    }

    // If using ContentManager, the local file URL should have been added to the asset URL map, so
    // we can request the asset URL for this file.
    try {
      return new URL(this.contentManager.getAssetUrl(filename));
    } catch (MalformedURLException e) {
      throw new FileNotFoundException(filename);
    }
  }

  @Override
  public void cleanUpTempDirectory(File tempFolder) throws IOException {
    this.dashboardFileManager.cleanUpTempDirectory(tempFolder);
  }
}
