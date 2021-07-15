package org.code.theater;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
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
    this.audioWriter = new AudioWriter.Factory().createAudioWriter(this.audioOutputStream);
  }

  public double[] readSampleSound()
      throws FileNotFoundException, SoundException, URISyntaxException {
    return this.read("beatbox.wav");
  }

  public double[] read(String audioFilename)
      throws FileNotFoundException, SoundException, URISyntaxException {
    final String fullAudioPath =
        Paths.get(CatMusic.class.getClassLoader().getResource(audioFilename).toURI()).toString();
    return SoundLoader.read(fullAudioPath);
  }

  public void playSound(double[] samples) {
    try {
      this.audioWriter.writeAudioSamples(samples);
    } catch (SoundException e) {
      e.printStackTrace();
    }
  }

  public void pause(double delaySeconds) throws SoundException {
    this.audioWriter.addDelay(delaySeconds);
  }

  public void play() throws SoundException {
    this.audioWriter.writeToAudioStreamAndClose();
    this.outputAdapter.sendMessage(SoundEncoder.encodeStreamToMessage(this.audioOutputStream));
  }

  public void reset() {
    this.audioWriter.reset();
  }
}
