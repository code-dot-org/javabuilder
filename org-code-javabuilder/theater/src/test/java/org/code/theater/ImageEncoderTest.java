package org.code.theater;

import java.awt.image.BufferedImage;
import org.code.protocol.ClientMessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ImageEncoderTest {
  @Test
  public void returnsTheaterMessageWithVisualKey() {
    TheaterMessage message = ImageEncoder.encodeImageToMessage(new BufferedImage(1, 1, 1));
    Assertions.assertEquals(message.getType(), ClientMessageType.THEATER);
    Assertions.assertEquals(message.getValue(), TheaterSignalKey.VISUAL.toString());
  }

  @Test
  public void returnsBase64Image() {
    TheaterMessage message = ImageEncoder.encodeImageToMessage(new BufferedImage(1, 1, 1));
    Assertions.assertTrue(message.getDetail().toString().contains("\"image\""));
    Assertions.assertTrue(
        message
            .getDetail()
            .toString()
            .contains("R0lGODlhAQABAPAAAAAAAAAAACwAAAAAAQABAEAIBAABBAQAOw=="));
  }
}
