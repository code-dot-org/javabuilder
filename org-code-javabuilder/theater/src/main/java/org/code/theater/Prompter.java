package org.code.theater;

import java.util.HashMap;
import org.code.media.Image;
import org.code.protocol.ClientMessageDetailKeys;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.OutputAdapter;

public class Prompter {
  private final OutputAdapter outputAdapter;

  // Used in Theater to create Prompter "singleton"
  // accessed by students.
  protected Prompter() {
    this(GlobalProtocol.getInstance().getOutputAdapter());
  }

  // Used to directly instantiate Prompter in tests.
  protected Prompter(OutputAdapter outputAdapter) {
    this.outputAdapter = outputAdapter;
  }

  public Image getImage(String prompt) {
    HashMap<String, String> getImageDetails = new HashMap<>();
    getImageDetails.put(ClientMessageDetailKeys.PROMPT, prompt);
    this.outputAdapter.sendMessage(new TheaterMessage(TheaterSignalKey.GET_IMAGE, getImageDetails));

    // TO DO: get provided image from Javalab.
    // https://codedotorg.atlassian.net/browse/CSA-936

    return null;
  }
}
