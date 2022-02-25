package org.code.protocol;

import java.io.FileNotFoundException;

/**
 * An {@link AssetFileHelper} that delegates to either the standard AssetFileHelper implementations
 * of its methods, or to a {@link ContentManager}. Used to temporarily support two code paths.
 */
public class DelegatingAssetFileHelper extends AssetFileHelper {

  private final ContentManager contentManager;
  private final boolean useDashboardSources;

  public DelegatingAssetFileHelper(
      String dashboardHostname,
      String channelId,
      String levelId,
      ContentManager contentManager,
      boolean useDashboardSources) {
    super(dashboardHostname, channelId, levelId);
    this.contentManager = contentManager;
    this.useDashboardSources = useDashboardSources;
  }

  @Override
  public String generateAssetUrl(String filename) {
    return this.useDashboardSources
        ? super.generateAssetUrl(filename)
        : this.contentManager.getAssetUrl(filename);
  }

  @Override
  public void verifyAssetFilename(String filename) throws FileNotFoundException {
    if (this.useDashboardSources) {
      super.verifyAssetFilename(filename);
    } else {
      this.contentManager.verifyAssetFilename(filename);
    }
  }
}
