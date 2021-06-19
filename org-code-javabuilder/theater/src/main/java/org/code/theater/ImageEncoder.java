package org.code.theater;

import com.itextpdf.io.codec.LZWCompressor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    // ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();
    // Somehow, this actually made the gif bigger...
    // LZWCompressor compressor = null;
    // try {
    //   compressor = new LZWCompressor(compressedStream, 8, true);
    //   compressor.compress(stream.toByteArray(), 0, stream.size());
    //   compressor.flush();
    // } catch (IOException e) {
    //   e.printStackTrace();
    // }

    System.out.print("Original stream length: ");
    System.out.println(stream.toByteArray().length);
    // System.out.print("New stream length: ");
    // System.out.println(compressedStream.toByteArray().length);
    String encodedString = Base64.getEncoder().encodeToString(stream.toByteArray());
    HashMap<String, String> message = new HashMap<>();
    message.put("image", encodedString);
    return new TheaterMessage(TheaterSignalKey.VISUAL, message);
  }
}
