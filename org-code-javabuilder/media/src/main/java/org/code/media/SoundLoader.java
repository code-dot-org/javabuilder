package org.code.media;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.InternalJavabuilderError;

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
    // Acquire AudioInputStream
    final AudioInputStream audioInputStream;
    try {
      final URL audioFileUrl = new URL(GlobalProtocol.getInstance().generateAssetUrl(filename));
      audioInputStream =
          AudioUtils.convertStreamToDefaultAudioFormat(
              AudioSystem.getAudioInputStream(audioFileUrl));
    } catch (IOException e) {
      throw new FileNotFoundException("Could not find file: " + filename);
    } catch (UnsupportedAudioFileException e) {
      throw new SoundException(SoundExceptionKeys.INVALID_AUDIO_FILE_FORMAT);
    }

    // Read audio file samples as bytes
    final byte[] bytes;
    try {
      bytes = audioInputStream.readAllBytes();
      audioInputStream.close();
    } catch (IOException e) {
      throw new InternalJavabuilderError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }

    return AudioUtils.convertByteArrayToDoubleArray(
        bytes, audioInputStream.getFormat().getChannels());
  }
}
