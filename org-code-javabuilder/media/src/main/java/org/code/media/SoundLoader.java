package org.code.media;

import java.io.FileNotFoundException;

public class SoundLoader {
  /**
   * Extracts audio samples from WAV or AU file into array
   *
   * @param filename the name of the audio file
   * @return the array of samples, at 44.1 kilohertz. This means that 441000 samples are played per
   *     second.
   * @throws SoundException if there is an error reading the file, or FileNotFoundException when the
   *     file cannot be found
   */
  public static double[] read(String filename) throws SoundException, FileNotFoundException {
    return AudioUtils.readSamplesFromAssetFile(filename);
  }
}
