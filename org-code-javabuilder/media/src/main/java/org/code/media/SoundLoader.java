package org.code.media;

import java.io.FileNotFoundException;
import org.code.media.support.SoundExceptionKeys;
import org.code.media.util.AudioUtils;

public class SoundLoader {
  /**
   * Extracts audio samples from WAV or AU file into array
   *
   * @param filename the name of the audio file
   * @return the array of samples, at 44.1 kilohertz. This means that 441000 samples are played per
   *     second.
   * @throws SoundException if there is an error reading the file
   */
  public static double[] read(String filename) throws SoundException {
    try {
      return AudioUtils.readSamplesFromAssetFile(filename);
    } catch (FileNotFoundException e) {
      throw new SoundException(SoundExceptionKeys.FILE_NOT_FOUND, e);
    }
  }
}
