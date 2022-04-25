package org.code.theater;

import static org.code.protocol.AllowedFileNames.PROMPTER_FILE_NAME_PREFIX;
import static org.code.protocol.InputMessages.UPLOAD_ERROR;
import static org.code.protocol.InputMessages.UPLOAD_SUCCESS;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.code.media.Image;
import org.code.protocol.*;
import org.code.theater.support.TheaterMessage;
import org.code.theater.support.TheaterSignalKey;

public class Prompter {
  private static final AtomicInteger FILE_INDEX = new AtomicInteger(0);

  // Convenience class just for unit testing
  static class ImageCreator {
    public Image createImage(String filename) throws FileNotFoundException {
      return new Image(filename);
    }
  }

  private final OutputAdapter outputAdapter;
  private final ContentManager contentManager;
  private final InputHandler inputHandler;
  private final ImageCreator imageCreator;

  // Used in Theater to create Prompter "singleton"
  // accessed by students.
  protected Prompter() {
    this(
        GlobalProtocol.getInstance().getOutputAdapter(),
        GlobalProtocol.getInstance().getContentManager(),
        GlobalProtocol.getInstance().getInputHandler(),
        new ImageCreator());
  }

  // Used to directly instantiate Prompter in tests.
  protected Prompter(
      OutputAdapter outputAdapter,
      ContentManager contentManager,
      InputHandler inputHandler,
      ImageCreator imageCreator) {
    this.outputAdapter = outputAdapter;
    this.contentManager = contentManager;
    this.inputHandler = inputHandler;
    this.imageCreator = imageCreator;
  }

  public Image getImage(String prompt) {
    final String prompterFileName = PROMPTER_FILE_NAME_PREFIX + FILE_INDEX.incrementAndGet();
    final String uploadUrl;
    try {
      uploadUrl = this.contentManager.generateAssetUploadUrl(prompterFileName);
    } catch (JavabuilderException e) {
      throw new InternalServerRuntimeException(InternalExceptionKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }

    HashMap<String, String> getImageDetails = new HashMap<>();
    getImageDetails.put(ClientMessageDetailKeys.PROMPT, prompt);
    getImageDetails.put(ClientMessageDetailKeys.UPLOAD_URL, uploadUrl);
    this.outputAdapter.sendMessage(new TheaterMessage(TheaterSignalKey.GET_IMAGE, getImageDetails));

    // Wait for an upload status message from Javalab
    final String statusMessage = this.inputHandler.getNextMessageForType(InputMessageType.THEATER);
    if (statusMessage.equals(UPLOAD_SUCCESS)) {
      try {
        return this.imageCreator.createImage(prompterFileName);
      } catch (FileNotFoundException e) {
        // If the image was uploaded successfully, a FileNotFoundException means an error on our end
        throw new InternalServerRuntimeException(
            InternalExceptionKey.INTERNAL_RUNTIME_EXCEPTION, e);
      }
    } else if (statusMessage.equals(UPLOAD_ERROR)) {
      throw new InternalServerRuntimeException(
          InternalExceptionKey.INTERNAL_RUNTIME_EXCEPTION, new Exception(UPLOAD_ERROR));
    } else {
      throw new InternalServerRuntimeException(InternalExceptionKey.UNKNOWN_ERROR);
    }
  }
}
