package org.code.media;

import static org.junit.jupiter.api.Assertions.*;

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
            SoundException.class,
            () -> {
              SoundLoader.read(null);
            });

    String expected = "Missing file name";
    assertTrue(exception.getMessage().contains(expected));
  }

  @Test
  public void testInvalidFileThrowsException() {
    Exception exception =
        assertThrows(
            SoundException.class,
            () -> {
              SoundLoader.read("invalid.wav");
            });

    String expected = "File does not exist";
    assertTrue(exception.getMessage().contains(expected));
  }

  @Test
  public void testReadsValidFileCorrectly() throws SoundException {
    double[] samples = SoundLoader.read(validFilepath);

    // Assert the full file was converted
    assertTrue(samples.length >= EXPECTED_TEST_AUDIO_DURATION_S * 44100);
  }
}
