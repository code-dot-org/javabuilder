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
  private final ByteArrayOutputStream audioOutputStream;
  private final ByteArrayOutputStream rawOutputStream;

  public AudioWriter(ByteArrayOutputStream audioOutputStream) {
    this(audioOutputStream, new ByteArrayOutputStream());
  }

  AudioWriter(ByteArrayOutputStream audioOutputStream, ByteArrayOutputStream rawOutputStream) {
    this.audioOutputStream = audioOutputStream;
    this.rawOutputStream = rawOutputStream;
  }

  public void writeAudioSamples(double[] samples) throws InternalJavabuilderError {
    final byte[] bytes = AudioUtils.convertDoubleArrayToByteArray(samples);
    try {
      this.rawOutputStream.write(bytes);
    } catch (IOException e) {
      throw new InternalJavabuilderError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }

  public void writeAudioFile(String filename)
      throws SoundException, InternalJavabuilderError, FileNotFoundException {
    this.writeAudioSamples(SoundLoader.read(filename));
  }

  public void addDelay(int delayMs) throws InternalJavabuilderError {
    final double[] emptySamples =
        new double[(int) (((double) delayMs / 1000.0) * AudioUtils.getDefaultSampleRate())];
    this.writeAudioSamples(emptySamples);
  }

  /**
   * Writes the raw audio data in rawOutputStream to audioOutputStream in a valid audio file format
   * and closes output streams.
   *
   * @throws InternalJavabuilderError
   */
  public void writeToAudioStreamAndClose() throws InternalJavabuilderError {
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
