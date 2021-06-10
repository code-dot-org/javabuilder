package org.code;

public enum Instrument {
  PIANO, BASS
}

public static class SoundLoader {
  /**
   * Extracts audio samples from WAV or AU file into array
   * 
   * @param filename the name of the audio file
   * @return the array of samples
   * @throws SoundException when the file cannot be found
   */
  public static double[] read(String filename) throws SoundException {
  }
}