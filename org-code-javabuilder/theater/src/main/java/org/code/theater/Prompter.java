package org.code.theater;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.code.media.Image;
import org.code.protocol.*;

public class Prompter {
  private static final String FILE_NAME = "prompterImage-";
  private static final AtomicInteger FILE_INDEX = new AtomicInteger(0);

  private final OutputAdapter outputAdapter;
  private final JavabuilderFileManager fileManager;

  // Used in Theater to create Prompter "singleton"
  // accessed by students.
  protected Prompter() {
    this(
        GlobalProtocol.getInstance().getOutputAdapter(),
        GlobalProtocol.getInstance().getFileManager());
  }

  // Used to directly instantiate Prompter in tests.
  protected Prompter(OutputAdapter outputAdapter, JavabuilderFileManager fileManager) {
    this.outputAdapter = outputAdapter;
    this.fileManager = fileManager;
  }

  public Image getImage(String prompt) {
    final String prompterFileName = FILE_NAME + FILE_INDEX.incrementAndGet();
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

    // TO DO: get provided image from Javalab.
    // https://codedotorg.atlassian.net/browse/CSA-936

    return null;
  }
}
