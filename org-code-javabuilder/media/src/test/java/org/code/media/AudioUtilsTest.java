package org.code.media;

import static org.junit.jupiter.api.Assertions.*;

import javax.sound.sampled.AudioFormat;
import org.junit.jupiter.api.Test;

class AudioUtilsTest {

  private static final double[] DOUBLE_ARRAY = {1.0, -1.0, 0.5, -0.5, 0.0};
  private static final byte[] BYTE_ARRAY_MONO = {
    (byte) 0xFF, (byte) 0x7F, // 1.0
    (byte) 0x00, (byte) 0x80, // -1.0
    (byte) 0x00, (byte) 0x40, // 0.5
    (byte) 0x00, (byte) 0xC0, // -0.5
    (byte) 0x00, (byte) 0x00 // 0.0
  };

  @Test
  public void testIsAudioFormatValidReturnsTrueForValidFormat() {
    AudioFormat valid =
        new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, -1, -1, false);
    assertTrue(AudioUtils.isAudioFormatValid(valid));
  }

  @Test
  public void testIsAudioFormatValidReturnsFalseForInvalidFormat() {
    AudioFormat invalid =
        new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000, 24, 6, -1, -1, true);
    assertFalse(AudioUtils.isAudioFormatValid(invalid));
  }

  @Test
  public void testConvertByteArrayThrowsIfArrayIsNull() {
    Exception exception =
        assertThrows(
            SoundException.class,
            () -> {
              AudioUtils.convertByteArrayToDoubleArray(null, 1);
            });

    assertTrue(exception.getMessage().contains("Cannot read audio data"));
  }

  @Test
  public void testConvertByteArrayThrowsIfInvalidNumberOfChannels() {
    Exception exception =
        assertThrows(
            SoundException.class,
            () -> {
              AudioUtils.convertByteArrayToDoubleArray(BYTE_ARRAY_MONO, 6);
            });

    assertTrue(exception.getMessage().contains("Invalid audio file format"));
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

    assertTrue(exception.getMessage().contains("Cannot read audio data"));
  }

  @Test
  public void testConvertDoubleArrayConvertsCorrectly() throws SoundException {
    assertArrayEquals(BYTE_ARRAY_MONO, AudioUtils.convertDoubleArrayToByteArray(DOUBLE_ARRAY));
  }
}
