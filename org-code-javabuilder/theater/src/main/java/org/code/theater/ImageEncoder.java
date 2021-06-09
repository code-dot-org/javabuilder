package org.code.theater;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import javax.imageio.ImageIO;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.InternalJavabuilderError;

public class ImageEncoder {
  public static TheaterMessage encodeImageToMessage(BufferedImage image) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      ImageIO.write(image, "gif", out);
    } catch (IOException e) {
      throw new InternalJavabuilderError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION);
    }

    String encodedString = Base64.getEncoder().encodeToString(out.toByteArray());
    HashMap<String, String> message = new HashMap<>();
    message.put("image", encodedString);
    return new TheaterMessage(TheaterSignalKey.VISUAL, message);
  }
}
