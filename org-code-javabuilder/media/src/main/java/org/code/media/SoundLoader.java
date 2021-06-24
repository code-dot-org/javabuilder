package org.code.media;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundLoader {
  /**
   * Extracts audio samples from WAV or AU file into array
   *
   * @param filename the name of the audio file
   * @return the array of samples, at 44.1 kilohertz. This means that 441000 samples are played per
   *     second.
   * @throws SoundException when the file cannot be found
   */
  public static double[] read(String filename) throws SoundException {
    // Acquire AudioInputStream
    final AudioInputStream audioInputStream = getAudioInputStream(filename);
    final AudioFormat audioFormat = audioInputStream.getFormat();
    if (!AudioUtils.isAudioFormatValid(audioFormat)) {
      throw new SoundException("Invalid audio file format");
    }

    // Read audio file samples as bytes
    final byte[] bytes;
    try {
      final int bytesToRead = audioInputStream.available();
      bytes = new byte[bytesToRead];
      final int bytesRead = audioInputStream.read(bytes);
      if (bytesToRead != bytesRead) {
        throw new SoundException("Error reading audio file");
      }
      audioInputStream.close();
    } catch (IOException e) {
      throw new SoundException(e);
    }

    return AudioUtils.convertByteArrayToDoubleArray(bytes, audioFormat.getChannels());
  }

  private static AudioInputStream getAudioInputStream(String filename) throws SoundException {
    if (filename == null) {
      throw new SoundException("Missing file name");
    }

    try {
      final File file = new File(filename);
      if (file.exists()) {
        return AudioSystem.getAudioInputStream(file);
      }
      throw new SoundException("File does not exist: " + filename);
    } catch (IOException | UnsupportedAudioFileException e) {
      throw new SoundException(e);
    }
  }
}
