package org.code.media;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

public class AudioWriterTest {
  @Test
  public void testWriteAddsSilentSoundIfEmpty() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    AudioWriter writer = new AudioWriter(stream);
    writer.writeToAudioStreamAndClose();
    byte[] result = stream.toByteArray();
    // The length is 46 because there is a lot of metadata before the data begins. The data are
    // zeros because we add in a single silent sound sample.
    assertEquals(46, result.length);
    assertEquals(0, result[result.length - 1]);
    assertEquals(0, result[result.length - 2]);
  }

  @Test
  public void testWriteUsesExistingAudioData() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    AudioWriter writer = new AudioWriter(stream);
    writer.writeAudioSamples(new double[] {1});
    writer.writeToAudioStreamAndClose();
    byte[] result = stream.toByteArray();
    // The exact values don't matter so much as ensuring they are something specific and not zero.
    // The length is 46 because there is a lot of metadata before the data begins.
    assertEquals(46, result.length);
    assertEquals(127, result[result.length - 1]);
    assertEquals(-1, result[result.length - 2]);
  }
}
