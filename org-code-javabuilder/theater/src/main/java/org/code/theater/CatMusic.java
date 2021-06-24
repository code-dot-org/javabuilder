package org.code.theater;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.code.media.AudioWriter;
import org.code.media.SoundException;
import org.code.media.SoundLoader;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.OutputAdapter;

/**
 * This is a sample class with some audio reading and writing capabilities to demonstrate how
 * Theater could use the audio API. This class is for demo purposes only and should be removed
 * before Theater is shipped.
 */
public class CatMusic {

  private final OutputAdapter outputAdapter;
  private final ByteArrayOutputStream audioOutputStream;
  private final AudioWriter audioWriter;

  public CatMusic() {
    this(GlobalProtocol.getInstance().getOutputAdapter(), new ByteArrayOutputStream());
  }

  CatMusic(OutputAdapter outputAdapter, ByteArrayOutputStream audioOutputStream) {
    this.outputAdapter = outputAdapter;
    this.audioOutputStream = audioOutputStream;
    this.audioWriter = new AudioWriter(this.audioOutputStream);
  }

  public double[] readSampleSound() {
    try {
      final String audioFilename =
          Paths.get(CatMusic.class.getClassLoader().getResource("beatbox.wav").toURI()).toString();
      return SoundLoader.read(audioFilename);
    } catch (URISyntaxException | SoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void playSound(double[] samples) {
    try {
      this.audioWriter.writeAudioSamples(samples);
    } catch (SoundException e) {
      e.printStackTrace();
    }
  }

  public void wait(int delayMs) {
    try {
      this.audioWriter.addDelay(delayMs);
    } catch (SoundException e) {
      e.printStackTrace();
    }
  }

  public void play() {
    try {
      this.audioWriter.writeToAudioStreamAndClose();
      this.outputAdapter.sendMessage(SoundEncoder.encodeStreamToMessage(this.audioOutputStream));
    } catch (SoundException e) {
      e.printStackTrace();
    }
  }

  public void reset() {
    this.audioWriter.reset();
  }
}
