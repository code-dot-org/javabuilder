package org.code.theater;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.code.media.Image;
import org.code.protocol.*;

public class Prompter {
  private static final String FILE_NAME = "prompterImage";
  private static final AtomicInteger FILE_INDEX = new AtomicInteger(0);

  private final OutputAdapter outputAdapter;
  private final JavabuilderUploadUrlGenerator urlGenerator;
  private final InputHandler inputHandler;

  // Used in Theater to create Prompter "singleton"
  // accessed by students.
  protected Prompter() {
    this(
        GlobalProtocol.getInstance().getOutputAdapter(),
        GlobalProtocol.getInstance().getUrlGenerator(),
        GlobalProtocol.getInstance().getInputHandler());
  }

  // Used to directly instantiate Prompter in tests.
  protected Prompter(
      OutputAdapter outputAdapter,
      JavabuilderUploadUrlGenerator urlGenerator,
      InputHandler inputHandler) {
    this.outputAdapter = outputAdapter;
    this.urlGenerator = urlGenerator;
    this.inputHandler = inputHandler;
  }

  public Image getImage(String prompt) {
    final String prompterFileName = FILE_NAME + "_" + FILE_INDEX.incrementAndGet();
    HashMap<String, String> getImageDetails = new HashMap<>();
    getImageDetails.put(ClientMessageDetailKeys.PROMPT, prompt);
    getImageDetails.put(
        ClientMessageDetailKeys.UPLOAD_URL, this.urlGenerator.getSignedUrl(prompterFileName));
    this.outputAdapter.sendMessage(new TheaterMessage(TheaterSignalKey.GET_IMAGE, getImageDetails));

    final String statusMessage = this.inputHandler.getNextMessageForType(InputMessageType.THEATER);
    if (statusMessage.equals("OK")) {
      final String url = this.urlGenerator.getFileUrl(prompterFileName);
      try {
        return new Image(Image.getImageFromUrl(url));
      } catch (FileNotFoundException e) {
        System.out.println(e);
      }
    }

    // TO DO: get provided image from Javalab.
    // https://codedotorg.atlassian.net/browse/CSA-936

    return null;
  }
}
