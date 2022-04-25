package org.code.media;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.code.protocol.InternalExceptionKey;
import org.code.protocol.InternalServerRuntimeException;

/**
 * Writer for concatenating audio data from multiple audio sources. The raw audio samples in bytes
 * are written to a raw output stream and finally written to the given audio output stream when
 * complete.
 */
public class AudioWriter {
  public static class Factory {
    public AudioWriter createAudioWriter(ByteArrayOutputStream audioOutputStream) {
      return new AudioWriter(audioOutputStream);
    }
  }

  private final ByteArrayOutputStream audioOutputStream;

  private double[] audioSamples;
  private int currentSampleIndex;

  AudioWriter(ByteArrayOutputStream audioOutputStream) {
    this.audioOutputStream = audioOutputStream;

    this.audioSamples = new double[] {};
    this.currentSampleIndex = 0;
  }

  public void writeAudioSamples(double[] samples) {
    this.audioSamples =
        AudioUtils.blendSamples(this.audioSamples, samples, this.currentSampleIndex);
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
    this.currentSampleIndex += (int) (delaySeconds * (double) AudioUtils.getDefaultSampleRate());
  }

  public double getTotalAudioLength() {
    return ((double) this.audioSamples.length) / (double) AudioUtils.getDefaultSampleRate();
  }

  /**
   * Writes the raw audio data in audioSamples to audioOutputStream in a valid audio file format and
   * closes output streams.
   */
  public void writeToAudioStream() {
    if (this.audioSamples.length == 0) {
      // Add a silent audio sample so we can build a valid wav file.
      // TODO: Send a "No audio" signal instead
      this.audioSamples = new double[] {0};
    }

    AudioUtils.writeBytesToOutputStream(
        AudioUtils.convertDoubleArrayToByteArray(this.audioSamples), this.audioOutputStream);
    this.currentSampleIndex = 0;
  }

  public void close() {
    try {
      this.audioOutputStream.close();
    } catch (IOException e) {
      throw new InternalServerRuntimeException(InternalExceptionKey.INTERNAL_EXCEPTION, e);
    }
  }
}
