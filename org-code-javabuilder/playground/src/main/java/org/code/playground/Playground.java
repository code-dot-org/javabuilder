package org.code.playground;

import java.util.HashMap;
import org.code.protocol.*;

public static final class Playground {
  public static final Board board = new Board();
  private final OutputAdapter outputAdapter;

  public Playground() {
    this.outputAdapter = GlobalProtocol.getInstance().getOutputAdapter();
  }

  public void sendTestMessages() {
    HashMap<String, String> details = new HashMap<>();
    details.put("filename", "puppy_1.jpg");
    details.put("width", "200");
    details.put("height", "200");
    details.put("x", "200");
    details.put("y", "200");
    details.put("id", "first_puppy");
    this.outputAdapter.sendMessage(new PlaygroundMessage(PlaygroundSignalKey.ADD_IMAGE, details));
  }
}
