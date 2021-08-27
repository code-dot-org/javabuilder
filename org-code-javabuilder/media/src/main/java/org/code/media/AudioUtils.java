package org.code.media;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import javax.sound.sampled.*;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.InternalServerRuntimeError;

class AudioUtils {
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
      throw new SoundException(SoundExceptionKeys.MISSING_AUDIO_DATA);
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
      throw new SoundException(SoundExceptionKeys.INVALID_AUDIO_FILE_FORMAT);
    }

    return samples;
  }

  /**
   * Converts an array of raw audio samples as doubles to raw audio samples in bytes. Each 16-bit
   * sample is converted to two bytes. Bitwise operations assume byte data is little-endian.
   *
   * @param samples
   * @return converted array of samples as bytes
   * @throws SoundException
   */
  public static byte[] convertDoubleArrayToByteArray(double[] samples) throws SoundException {
    if (samples == null) {
      throw new SoundException(SoundExceptionKeys.MISSING_AUDIO_DATA);
    }
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
   */
  public static void writeBytesToOutputStream(byte[] bytes, ByteArrayOutputStream outputStream) {
    final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
    final AudioInputStream audioInputStream =
        new AudioInputStream(inputStream, DEFAULT_AUDIO_FORMAT, bytes.length / 2);

    try {
      AudioSystem.write(audioInputStream, DEFAULT_AUDIO_FILE_FORMAT_TYPE, outputStream);
    } catch (IOException e) {
      throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }

  /**
   * Truncates the array of audio samples to the desired duration in seconds provided. If the
   * desired duration is greater than the duration of the samples, the samples are unchanged.
   *
   * @param samples
   * @param lengthSeconds
   * @return truncated samples
   */
  public static double[] truncateSamples(double[] samples, double lengthSeconds) {
    final int newLength = (int) (lengthSeconds * (double) DEFAULT_SAMPLE_RATE);
    if (newLength > samples.length) {
      return samples;
    }
    return Arrays.copyOf(samples, newLength);
  }

  /**
   * Blends audio samples from newSamples into originalSamples starting at the sample index
   * indicated by sampleOffset
   *
   * @param originalSamples original samples to blend into
   * @param newSamples new samples to blend
   * @param sampleOffset sample index to start blending
   * @return
   */
  public static double[] blendSamples(
      double[] originalSamples, double[] newSamples, int sampleOffset) {
    if (originalSamples == null || newSamples == null) {
      throw new SoundException(SoundExceptionKeys.MISSING_AUDIO_DATA);
    }
    final double[] blendedSamples =
        Arrays.copyOf(
            originalSamples, Math.max(originalSamples.length, sampleOffset + newSamples.length));

    for (int i = 0; i < newSamples.length; i++) {
      // Blend samples by adding and clamping to range (-1.0, 1.0)
      final double blendedSample = blendedSamples[i + sampleOffset] + newSamples[i];
      blendedSamples[i + sampleOffset] = Math.max(-1.0, Math.min(1.0, blendedSample));
    }

    return blendedSamples;
  }

  public static int getDefaultSampleRate() {
    return DEFAULT_SAMPLE_RATE;
  }

  /**
   * Loads and reads the audio samples from the given asset file
   *
   * @param filename Name of the asset file
   * @return samples
   * @throws FileNotFoundException
   */
  public static double[] readSamplesFromAssetFile(String filename) throws FileNotFoundException {
    try {
      final URL audioFileUrl = new URL(GlobalProtocol.getInstance().generateAssetUrl(filename));
      final AudioInputStream audioInputStream =
          AudioUtils.convertStreamToDefaultAudioFormat(
              AudioSystem.getAudioInputStream(audioFileUrl));
      return AudioUtils.readSamplesFromInputStream(audioInputStream);
    } catch (IOException e) {
      throw new FileNotFoundException("Could not find file: " + filename);
    } catch (UnsupportedAudioFileException e) {
      throw new SoundException(SoundExceptionKeys.INVALID_AUDIO_FILE_FORMAT);
    }
  }

  /**
   * Loads and reads the audio samples from the file referenced by the given local filepath. Meant
   * for internal use.
   *
   * @param filepath local path
   * @return samples
   * @throws FileNotFoundException
   */
  static double[] readSamplesFromLocalFile(String filepath) throws FileNotFoundException {
    try {
      return AudioUtils.readSamplesFromInputStream(
          AudioUtils.convertStreamToDefaultAudioFormat(
              AudioSystem.getAudioInputStream(new File(filepath))));
    } catch (IOException e) {
      throw new FileNotFoundException("Could not find file: " + filepath);
    } catch (UnsupportedAudioFileException e) {
      throw new SoundException(SoundExceptionKeys.INVALID_AUDIO_FILE_FORMAT);
    }
  }

  private static double[] readSamplesFromInputStream(AudioInputStream audioInputStream) {
    final byte[] bytes;
    try {
      bytes = audioInputStream.readAllBytes();
      audioInputStream.close();
    } catch (IOException e) {
      throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }

    return AudioUtils.convertByteArrayToDoubleArray(
        bytes, audioInputStream.getFormat().getChannels());
  }

  private static AudioInputStream convertStreamToDefaultAudioFormat(AudioInputStream stream)
      throws SoundException {
    if (!AudioSystem.isConversionSupported(DEFAULT_AUDIO_FORMAT, stream.getFormat())) {
      throw new SoundException(SoundExceptionKeys.INVALID_AUDIO_FILE_FORMAT);
    }

    return AudioSystem.getAudioInputStream(DEFAULT_AUDIO_FORMAT, stream);
  }
}
