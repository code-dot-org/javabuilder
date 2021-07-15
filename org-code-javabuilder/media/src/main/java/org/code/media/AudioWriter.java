package org.code.media;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.InternalJavabuilderError;

/**
 * Writer for concatenating audio data from multiple audio sources. The raw audio samples in bytes
 * are written to a raw output stream and finally written to the given audio output stream when
 * complete.
 */
public class AudioWriter {
  public static class Factory {
    public AudioWriter createAudioWriter(ByteArrayOutputStream audioOutputStream) {
      return new AudioWriter(audioOutputStream, new ByteArrayOutputStream());
    }
  }

  private final ByteArrayOutputStream audioOutputStream;
  private final ByteArrayOutputStream rawOutputStream;

  AudioWriter(ByteArrayOutputStream audioOutputStream, ByteArrayOutputStream rawOutputStream) {
    this.audioOutputStream = audioOutputStream;
    this.rawOutputStream = rawOutputStream;
  }

  public void writeAudioSamples(double[] samples) {
    final byte[] bytes = AudioUtils.convertDoubleArrayToByteArray(samples);
    try {
      this.rawOutputStream.write(bytes);
    } catch (IOException e) {
      throw new InternalJavabuilderError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }

  public void writeAudioSamples(double[] samples, double lengthSeconds) {
    this.writeAudioSamples(AudioUtils.truncateSamples(samples, lengthSeconds));
  }

  public void writeAudioFromAssetFile(String filename)
      throws SoundException, FileNotFoundException {
    this.writeAudioSamples(AudioUtils.readSamplesFromAssetFile(filename));
  }

  public void writeAudioFromAssetFile(String filename, double lengthSeconds)
      throws SoundException, FileNotFoundException {
    this.writeAudioSamples(AudioUtils.readSamplesFromAssetFile(filename), lengthSeconds);
  }

  public void writeAudioFromLocalFile(String filepath)
      throws SoundException, FileNotFoundException {
    this.writeAudioSamples(AudioUtils.readSamplesFromLocalFile(filepath));
  }

  public void writeAudioFromLocalFile(String filepath, double lengthSeconds)
      throws SoundException, FileNotFoundException {
    this.writeAudioSamples(AudioUtils.readSamplesFromLocalFile(filepath), lengthSeconds);
  }

  public void addDelay(double delaySeconds) {
    final double[] emptySamples =
        new double[(int) (delaySeconds * (double) AudioUtils.getDefaultSampleRate())];
    this.writeAudioSamples(emptySamples);
  }

  /**
   * Writes the raw audio data in rawOutputStream to audioOutputStream in a valid audio file format
   * and closes output streams.
   */
  public void writeToAudioStreamAndClose() {
    final byte[] bytes = this.rawOutputStream.toByteArray();
    AudioUtils.writeBytesToOutputStream(bytes, this.audioOutputStream);

    try {
      this.rawOutputStream.close();
      this.audioOutputStream.close();
    } catch (IOException e) {
      throw new InternalJavabuilderError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }

  public void reset() {
    this.rawOutputStream.reset();
    this.audioOutputStream.reset();
  }
}
