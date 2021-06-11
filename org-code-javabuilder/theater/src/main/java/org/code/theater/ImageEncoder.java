package org.code.theater;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;

public class ImageEncoder {
  /**
   * Takes a ByteArrayOutputStream representing an image and convert it to an encoding that can be
   * passed across the WebSocket connection. We expect visual elements to be gifs in a base64
   * encoding
   *
   * @param stream the stream to encode
   * @return a TheaterMessage with Value "VISUAL" and detail set to {image: "base64EncodedImage"}
   */
  public static TheaterMessage encodeStreamToMessage(ByteArrayOutputStream stream) {
    String encodedString = Base64.getEncoder().encodeToString(stream.toByteArray());
    HashMap<String, String> message = new HashMap<>();
    message.put("image", encodedString);
    return new TheaterMessage(TheaterSignalKey.VISUAL, message);
  }
}
