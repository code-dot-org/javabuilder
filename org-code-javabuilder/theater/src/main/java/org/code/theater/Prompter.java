package org.code.theater;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.code.media.Image;
import org.code.protocol.ClientMessageDetailKeys;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.JavabuilderUploadUrlGenerator;
import org.code.protocol.OutputAdapter;

public class Prompter {
  private static final String FILE_NAME = "prompterImage";
  private static final AtomicInteger FILE_INDEX = new AtomicInteger(1);

  private final OutputAdapter outputAdapter;
  private final JavabuilderUploadUrlGenerator urlGenerator;

  // Used in Theater to create Prompter "singleton"
  // accessed by students.
  protected Prompter() {
    this(
        GlobalProtocol.getInstance().getOutputAdapter(),
        GlobalProtocol.getInstance().getUrlGenerator());
  }

  // Used to directly instantiate Prompter in tests.
  protected Prompter(OutputAdapter outputAdapter, JavabuilderUploadUrlGenerator urlGenerator) {
    this.outputAdapter = outputAdapter;
    this.urlGenerator = urlGenerator;
  }

  public Image getImage(String prompt) {
    HashMap<String, String> getImageDetails = new HashMap<>();
    getImageDetails.put(ClientMessageDetailKeys.PROMPT, prompt);
    getImageDetails.put(
        ClientMessageDetailKeys.UPLOAD_URL,
        this.urlGenerator.getSignedUrl(FILE_NAME + "_" + FILE_INDEX.incrementAndGet()));
    this.outputAdapter.sendMessage(new TheaterMessage(TheaterSignalKey.GET_IMAGE, getImageDetails));

    // TO DO: get provided image from Javalab.
    // https://codedotorg.atlassian.net/browse/CSA-936

    return null;
  }
}
