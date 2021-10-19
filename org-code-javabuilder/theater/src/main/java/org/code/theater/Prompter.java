package org.code.theater;

import java.util.HashMap;
import org.code.media.Image;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.OutputAdapter;

public class Prompter {
  private final OutputAdapter outputAdapter;

  protected Prompter() {
    this.outputAdapter = GlobalProtocol.getInstance().getOutputAdapter();
  }

  public Image getImage(String prompt) {
    HashMap<String, String> getImageDetails = new HashMap<>();
    getImageDetails.put("prompt", prompt);
    this.outputAdapter.sendMessage(new TheaterMessage(TheaterSignalKey.GET_IMAGE, getImageDetails));

    return null;
  }
}
