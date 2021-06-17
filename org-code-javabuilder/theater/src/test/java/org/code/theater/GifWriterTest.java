package org.code.theater;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GifWriterTest {
  @Test
  public void writesToStream() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    GifWriter writer = new GifWriter(stream);
    writer.writeToGif(new BufferedImage(1, 1, 1), 1000);
    writer.writeToGif(new BufferedImage(1, 1, 1), 1000);
    writer.close();
    Assertions.assertNotEquals(0, stream.size());
  }
}
