package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssetFileStubberTest {

  private static final String IMAGE_URL = "file:/image.jpg";
  private static final String AUDIO_URL = "file:/audio.wav";

  private AssetFileStubber unitUnderTest;

  @BeforeEach
  public void setUp() {
    unitUnderTest = new AssetFileStubber(IMAGE_URL, AUDIO_URL);
  }

  @Test
  public void testReturnsImageUrlForImageFile() {
    final String[] files = {"file.jpg", "file.jpeg", "file.png", "file.gif"};
    for (String file : files) {
      assertEquals(IMAGE_URL, unitUnderTest.getStubAssetUrl(file));
    }
  }

  @Test
  public void testReturnsAudioUrlForAudioFile() {
    assertEquals(AUDIO_URL, unitUnderTest.getStubAssetUrl("file.wav"));
  }

  @Test
  public void testReturnsNullForUnknownFileType() {
    assertNull(unitUnderTest.getStubAssetUrl("file.pdf"));
  }
}
