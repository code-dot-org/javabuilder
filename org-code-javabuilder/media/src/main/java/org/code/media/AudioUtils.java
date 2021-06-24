package org.code.media;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class AudioUtils {
  private static final int MONO_CHANNELS = 1;
  private static final int STEREO_CHANNELS = 2;
  private static final double MAX_16_BIT_VALUE = 32768; // Max signed 16-bit value

  // Defaults for reading and writing audio files
  private static final int DEFAULT_SAMPLE_RATE = 44100;
  private static final int DEFAULT_BITS_PER_SAMPLE = 16;
  private static final int DEFAULT_OUTPUT_CHANNELS = MONO_CHANNELS;
  private static final boolean IS_SIGNED = true;
  private static final boolean IS_NOT_BIG_ENDIAN = false;

  private static final AudioFormat DEFAULT_AUDIO_FORMAT =
      new AudioFormat(
          DEFAULT_SAMPLE_RATE,
          DEFAULT_BITS_PER_SAMPLE,
          DEFAULT_OUTPUT_CHANNELS,
          IS_SIGNED,
          IS_NOT_BIG_ENDIAN);

  private static final AudioFileFormat.Type DEFAULT_AUDIO_FILE_FORMAT_TYPE =
      AudioFileFormat.Type.WAVE;

  /**
   * Determines if given {@link AudioFormat} is a valid format we can read
   *
   * @param audioFormat
   * @return if audio format is valid
   */
  public static boolean isAudioFormatValid(AudioFormat audioFormat) {
    return audioFormat.getSampleRate() == DEFAULT_SAMPLE_RATE
        && audioFormat.getSampleSizeInBits() == DEFAULT_BITS_PER_SAMPLE
        && audioFormat.isBigEndian() == IS_NOT_BIG_ENDIAN;
  }

  /**
   * Converts an array of raw audio samples as bytes to raw audio samples as doubles. Each sample is
   * normalized to be within the range (-1.0, 1.0). If byte array represents stereo sound (2
   * channels), left and right channels are averaged. Bitwise operations assume the byte data is
   * little-endian.
   *
   * @param bytes
   * @return converted array of samples as doubles
   * @throws SoundException
   */
  public static double[] convertByteArrayToDoubleArray(byte[] bytes, int numChannels)
      throws SoundException {
    if (bytes == null) {
      throw new SoundException("Cannot read audio data");
    }
    final int numBytes = bytes.length;
    final double[] samples;
    if (numChannels == MONO_CHANNELS) {
      // Converts two bytes to single 16-bit value divided by max 16-bit value to obtain normalized
      // value of sample.
      samples = new double[numBytes / 2];
      for (int i = 0; i < numBytes / 2; i++) {
        samples[i] =
            ((short) (((bytes[2 * i + 1] & 0xFF) << 8) | (bytes[2 * i] & 0xFF))) / MAX_16_BIT_VALUE;
      }
    } else if (numChannels == STEREO_CHANNELS) {
      // Converts two bytes for each channel (left and right) into 16-bit values divided by max
      // 16-bit value,
      // and then averages two channel values for final normalized decimal value of sample.
      samples = new double[numBytes / 4];
      for (int i = 0; i < numBytes / 4; i++) {
        final double left =
            ((short) (((bytes[4 * i + 1] & 0xFF) << 8) | (bytes[4 * i + 0] & 0xFF)))
                / MAX_16_BIT_VALUE;
        final double right =
            ((short) (((bytes[4 * i + 3] & 0xFF) << 8) | (bytes[4 * i + 2] & 0xFF)))
                / MAX_16_BIT_VALUE;
        samples[i] = (left + right) / 2.0;
      }
    } else {
      throw new SoundException("Invalid audio file format");
    }

    return samples;
  }

  /**
   * Converts an array of raw audio samples as doubles to raw audio samples in bytes. Each 16-bit
   * sample is converted to two bytes. Bitwise operations assume byte data is little-endian.
   *
   * @param samples
   * @return converted array of samples as bytes
   */
  public static byte[] convertDoubleArrayToByteArray(double[] samples) {
    final byte[] bytes = new byte[samples.length * 2];
    for (int i = 0; i < samples.length; i++) {
      final int b = samples[i] == 1.0 ? Short.MAX_VALUE : (short) (samples[i] * MAX_16_BIT_VALUE);
      bytes[2 * i] = (byte) (b & 0xFF);
      bytes[2 * i + 1] = (byte) ((b >> 8) & 0xFF);
    }
    return bytes;
  }

  /**
   * Writes the given raw audio byte array to the given {@link ByteArrayOutputStream} in a valid
   * audio file format
   *
   * @param bytes
   * @param outputStream
   * @throws SoundException
   */
  public static void writeBytesToOutputStream(byte[] bytes, ByteArrayOutputStream outputStream)
      throws SoundException {
    final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
    final AudioInputStream audioInputStream =
        new AudioInputStream(inputStream, DEFAULT_AUDIO_FORMAT, bytes.length / 2);

    try {
      AudioSystem.write(audioInputStream, DEFAULT_AUDIO_FILE_FORMAT_TYPE, outputStream);
    } catch (IOException e) {
      throw new SoundException(e);
    }
  }

  public static int getDefaultSampleRate() {
    return DEFAULT_SAMPLE_RATE;
  }
}
