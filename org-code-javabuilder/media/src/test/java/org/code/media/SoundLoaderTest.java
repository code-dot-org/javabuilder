package org.code.media;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SoundLoaderTest {

  private static final String TEST_FILE_NAME = "beatbox.wav";
  private static final double EXPECTED_TEST_AUDIO_DURATION_S = 2.3;
  private String validFilepath;

  @BeforeEach
  public void setUp() throws URISyntaxException {
    validFilepath =
        Paths.get(SoundLoaderTest.class.getClassLoader().getResource(TEST_FILE_NAME).toURI())
            .toString();
  }

  @Test
  public void testNullFilenameThrowsException() {
    Exception exception =
        assertThrows(
            FileNotFoundException.class,
            () -> {
              SoundLoader.read(null);
            });

    String expected = "Missing file name";
    assertTrue(exception.getMessage().contains(expected));
  }

  @Test
  public void testInvalidFileThrowsException() {
    String invalidFile = "invalid.wav";
    Exception exception =
        assertThrows(
            FileNotFoundException.class,
            () -> {
              SoundLoader.read(invalidFile);
            });

    assertTrue(exception.getMessage().contains(invalidFile));
  }

  @Test
  public void testReadsValidFileCorrectly() throws SoundException, FileNotFoundException {
    double[] samples = SoundLoader.read(validFilepath);

    // Assert the full file was converted
    assertTrue(samples.length >= EXPECTED_TEST_AUDIO_DURATION_S * 44100);
  }
}
