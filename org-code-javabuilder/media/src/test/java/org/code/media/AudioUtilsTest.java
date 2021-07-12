package org.code.media;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AudioUtilsTest {

  private static final double[] DOUBLE_ARRAY = {1.0, -1.0, 0.5, -0.5, 0.0};
  private static final byte[] BYTE_ARRAY_MONO = {
    (byte) 0xFF, (byte) 0x7F, // 1.0
    (byte) 0x00, (byte) 0x80, // -1.0
    (byte) 0x00, (byte) 0x40, // 0.5
    (byte) 0x00, (byte) 0xC0, // -0.5
    (byte) 0x00, (byte) 0x00 // 0.0
  };

  private MockedStatic<AudioSystem> audioSystem;
  private AudioInputStream audioInputStream;
  private AudioInputStream convertedStream;
  private AudioFormat audioFormat;

  @BeforeEach
  public void setUp() {
    audioSystem = mockStatic(AudioSystem.class);
    audioInputStream = mock(AudioInputStream.class);
    convertedStream = mock(AudioInputStream.class);
    audioFormat = mock(AudioFormat.class);
    when(audioInputStream.getFormat()).thenReturn(audioFormat);
  }

  @AfterEach
  public void tearDown() {
    audioSystem.close();
  }

  @Test
  public void testConvertStreamToDefaultAudioFormatThrowsIfNotSupported() {
    audioSystem
        .when(() -> AudioSystem.isConversionSupported(any(AudioFormat.class), eq(audioFormat)))
        .thenReturn(false);

    Exception exception =
        assertThrows(
            SoundException.class,
            () -> {
              AudioUtils.convertStreamToDefaultAudioFormat(audioInputStream);
            });

    assertEquals(SoundExceptionKeys.INVALID_AUDIO_FILE_FORMAT.toString(), exception.getMessage());
  }

  @Test
  public void testConvertStreamToDefaultAudioFormatReturnsConvertedStream() {
    audioSystem
        .when(() -> AudioSystem.isConversionSupported(any(AudioFormat.class), eq(audioFormat)))
        .thenReturn(true);
    audioSystem
        .when(() -> AudioSystem.getAudioInputStream(any(AudioFormat.class), eq(audioInputStream)))
        .thenReturn(convertedStream);

    assertEquals(convertedStream, AudioUtils.convertStreamToDefaultAudioFormat(audioInputStream));
  }

  @Test
  public void testConvertByteArrayThrowsIfArrayIsNull() {
    Exception exception =
        assertThrows(
            SoundException.class,
            () -> {
              AudioUtils.convertByteArrayToDoubleArray(null, 1);
            });

    assertEquals(exception.getMessage(), SoundExceptionKeys.MISSING_AUDIO_DATA.toString());
  }

  @Test
  public void testConvertByteArrayThrowsIfInvalidNumberOfChannels() {
    Exception exception =
        assertThrows(
            SoundException.class,
            () -> {
              AudioUtils.convertByteArrayToDoubleArray(BYTE_ARRAY_MONO, 6);
            });

    assertEquals(exception.getMessage(), SoundExceptionKeys.INVALID_AUDIO_FILE_FORMAT.toString());
  }

  @Test
  public void testConvertByteArrayConvertsCorrectly() throws SoundException {
    double[] converted = AudioUtils.convertByteArrayToDoubleArray(BYTE_ARRAY_MONO, 1);
    // Verify values are correct when rounded
    for (int i = 0; i < converted.length; i++) {
      assertEquals(DOUBLE_ARRAY[i], Math.round(converted[i] * 10) / 10.0);
    }
  }

  @Test
  public void testConvertDoubleArrayThrowsIfArrayIsNull() {
    Exception exception =
        assertThrows(
            SoundException.class,
            () -> {
              AudioUtils.convertDoubleArrayToByteArray(null);
            });

    assertEquals(exception.getMessage(), SoundExceptionKeys.MISSING_AUDIO_DATA.toString());
  }

  @Test
  public void testConvertDoubleArrayConvertsCorrectly() throws SoundException {
    assertArrayEquals(BYTE_ARRAY_MONO, AudioUtils.convertDoubleArrayToByteArray(DOUBLE_ARRAY));
  }

  @Test
  public void testTruncateSamplesTruncatesCorrectly() {
    final double[] samples = new double[5 * 44100]; // 5 second audio
    Arrays.fill(samples, 0.5);

    final double[] expected = new double[3 * 44100];
    Arrays.fill(expected, 0.5);
    assertArrayEquals(expected, AudioUtils.truncateSamples(samples, 3.0));
  }

  @Test
  public void testTruncateSamplesDoesNothingIfAudioShorterThanRequestedLength() {
    final double[] samples = new double[5 * 44100]; // 5 second audio
    Arrays.fill(samples, 0.5);

    assertSame(samples, AudioUtils.truncateSamples(samples, 7.0));
  }
}
