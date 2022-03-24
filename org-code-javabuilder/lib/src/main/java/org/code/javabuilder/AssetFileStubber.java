package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.util.logging.Logger;

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
    System.out.printf(
        "Generated stubbed URLs for image: %s and audio: %s\n", stubImageUrl, stubAudioUrl);
  }

  public String getStubAssetUrl(String filename) {
    System.out.println("Stubbing asset file: " + filename);
    if (this.matchesExtension(filename, IMAGE_EXTENSIONS)) {
      System.out.printf("Returning stubbed URL %s\n", stubImageUrl);
      return stubImageUrl;
    }

    if (this.matchesExtension(filename, AUDIO_EXTENSIONS)) {
      System.out.printf("Returning stubbed URL %s\n", stubAudioUrl);
      return stubAudioUrl;
    }

    System.out.println("Unknown extension; returning null");
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
