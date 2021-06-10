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
  /**
   * Takes a buffered image and converts it to an encoding that can be passed across the WebSocket
   * connection. We expect visual elements to be gifs in a base64 encoding
   *
   * @param image the image to encode
   * @return a TheaterMessage with Value "VISUAL" and detail set to {image: "base64EncodedImage"}
   */
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
