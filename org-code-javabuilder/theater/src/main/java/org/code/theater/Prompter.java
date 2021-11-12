package org.code.theater;

import static org.code.protocol.InputMessages.UPLOAD_ERROR;
import static org.code.protocol.InputMessages.UPLOAD_SUCCESS;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.code.media.Image;
import org.code.protocol.*;

public class Prompter {
  private static final String FILE_NAME_PREFIX = "prompterImage-";
  private static final AtomicInteger FILE_INDEX = new AtomicInteger(0);

  private final OutputAdapter outputAdapter;
  private final JavabuilderFileManager fileManager;
  private final InputHandler inputHandler;

  // Used in Theater to create Prompter "singleton"
  // accessed by students.
  protected Prompter() {
    this(
        GlobalProtocol.getInstance().getOutputAdapter(),
        GlobalProtocol.getInstance().getFileManager(),
        GlobalProtocol.getInstance().getInputHandler());
  }

  // Used to directly instantiate Prompter in tests.
  protected Prompter(
      OutputAdapter outputAdapter, JavabuilderFileManager fileManager, InputHandler inputHandler) {
    this.outputAdapter = outputAdapter;
    this.fileManager = fileManager;
    this.inputHandler = inputHandler;
  }

  public Image getImage(String prompt) {
    final String prompterFileName = FILE_NAME_PREFIX + FILE_INDEX.incrementAndGet();
    final String uploadUrl;
    try {
      uploadUrl = this.fileManager.getUploadUrl(prompterFileName);
    } catch (JavabuilderException e) {
      throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }

    HashMap<String, String> getImageDetails = new HashMap<>();
    getImageDetails.put(ClientMessageDetailKeys.PROMPT, prompt);
    getImageDetails.put(ClientMessageDetailKeys.UPLOAD_URL, uploadUrl);
    this.outputAdapter.sendMessage(new TheaterMessage(TheaterSignalKey.GET_IMAGE, getImageDetails));

    // Wait for an upload status message from Javalab
    final String statusMessage = this.inputHandler.getNextMessageForType(InputMessageType.THEATER);
    if (statusMessage.equals(UPLOAD_SUCCESS)) {
      try {
        return Image.fromUrl(this.fileManager.getFileUrl(prompterFileName));
      } catch (FileNotFoundException e) {
        // If the image was uploaded successfully, a FileNotFoundException indicates an error on our
        // end
        throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
      }
    } else if (statusMessage.equals(UPLOAD_ERROR)) {
      throw new InternalServerRuntimeError(
          InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, new Exception(UPLOAD_ERROR));
    } else {
      throw new InternalServerRuntimeError(InternalErrorKey.UNKNOWN_ERROR);
    }
  }
}
