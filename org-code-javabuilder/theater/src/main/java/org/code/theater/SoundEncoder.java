package org.code.theater;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;

public class SoundEncoder {

  public static TheaterMessage encodeStreamToMessage(ByteArrayOutputStream stream) {
    final String encodedString = Base64.getEncoder().encodeToString(stream.toByteArray());
    final HashMap<String, String> message = new HashMap<>();
    message.put("audio", encodedString);
    return new TheaterMessage(TheaterSignalKey.AUDIO, message);
  }
}
