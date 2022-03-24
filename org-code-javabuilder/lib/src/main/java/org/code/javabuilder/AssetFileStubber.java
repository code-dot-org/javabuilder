package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.util.logging.Logger;

/**
 * Generates URLs for local, static asset files that can be used to stub asset file requests when
 * URLs cannot be accessed, notably in the cases where asset URLs point to Dashboard running on
 * localhost, or during an integration test.
 */
public class AssetFileStubber {
  private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};
  private static final String[] AUDIO_EXTENSIONS = {".wav"};
  private static final String STUB_IMAGE_FILE_NAME = "sampleImageBeach.jpg";
  private static final String STUB_AUDIO_FILE_NAME = "beatbox.wav";

  private final String stubImageUrl;
  private final String stubAudioUrl;

  public AssetFileStubber() {
    stubImageUrl = getClass().getClassLoader().getResource(STUB_IMAGE_FILE_NAME).toString();
    stubAudioUrl = getClass().getClassLoader().getResource(STUB_AUDIO_FILE_NAME).toString();
  }

  // Visible for testing
  AssetFileStubber(String stubImageUrl, String stubAudioUrl) {
    this.stubImageUrl = stubImageUrl;
    this.stubAudioUrl = stubAudioUrl;
  }

  public String getStubAssetUrl(String filename) {
    if (this.matchesExtension(filename, IMAGE_EXTENSIONS)) {
      return stubImageUrl;
    }

    if (this.matchesExtension(filename, AUDIO_EXTENSIONS)) {
      return stubAudioUrl;
    }

    Logger.getLogger(MAIN_LOGGER)
        .warning(String.format("Unknown file %s. Cannot provide stubbed asset URL.", filename));
    return null;
  }

  private boolean matchesExtension(String filename, String[] extensions) {
    for (String extension : extensions) {
      if (filename.toLowerCase().endsWith(extension)) {
        return true;
      }
    }
    return false;
  }
}
