package org.code.theater;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.code.protocol.ClientMessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ImageEncoderTest {
  @Test
  public void returnsTheaterMessageWithVisualKey() {
    TheaterMessage message = ImageEncoder.encodeStreamToMessage(new ByteArrayOutputStream());
    Assertions.assertEquals(message.getType(), ClientMessageType.THEATER);
    Assertions.assertEquals(message.getValue(), TheaterSignalKey.VISUAL.toString());
  }

  @Test
  public void returnsBase64Image() throws IOException {
    BufferedImage testImage = new BufferedImage(1, 1, 1);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(testImage, "gif", outputStream);
    TheaterMessage message = ImageEncoder.encodeStreamToMessage(outputStream);
    Assertions.assertTrue(message.getDetail().toString().contains("\"image\""));
    Assertions.assertTrue(
        message
            .getDetail()
            .toString()
            // This is the base 64 encoded string of the buffered image
            .contains("R0lGODlhAQABAPAAAAAAAAAAACwAAAAAAQABAEAIBAABBAQAOw=="));
  }
}
