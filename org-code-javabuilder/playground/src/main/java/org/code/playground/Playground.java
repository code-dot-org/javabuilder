package org.code.playground;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import org.code.media.AudioWriter;
import org.code.media.Image;
import org.code.protocol.*;
import org.json.JSONObject;

public final class Playground {

  private static final Playground playgroundInstance = new Playground();

  public static Playground getInstance() {
    return Playground.playgroundInstance;
  }

  private static class ImagePosition {
    final int x;
    final int y;
    final int width;
    final int height;

    public ImagePosition(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }

    public boolean containsCoordinates(int x, int y) {
      return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }
  }

  private static final int PLAYGROUND_WIDTH = 400;
  private static final int PLAYGROUND_HEIGHT = 400;
  private static final String OUTPUT_IMAGE_FILE_FORMAT = "JPEG";
  private static final String OUTPUT_IMAGE_CONTENT_TYPE = "image/jpeg";
  private static final String OUTPUT_AUDIO_CONTENT_TYPE = "audio/wav";
  private static final String PLAYGROUND_IMAGE_FILE_NAME = "playgroundImage.jpeg";
  private static final String PLAYGROUND_AUDIO_FILE_NAME = "playgroundAudio.wav";

  private final OutputAdapter outputAdapter;
  private final InputHandler inputHandler;
  private final JavabuilderFileWriter fileWriter;
  private final AudioWriter audioWriter;
  private final BufferedImage image;
  private final Graphics2D graphics;
  private final Map<Image, ImagePosition> imagePositionMap;
  private final ArrayList<Image> imageDrawOrderStack;

  private boolean isRunning;
  private boolean updateRequested;
  private boolean exitRequested;
  private Image backgroundImage;
  private String soundFilename;
  private String exitSound;

  public Playground() {
    this.outputAdapter = GlobalProtocol.getInstance().getOutputAdapter();
    this.inputHandler = GlobalProtocol.getInstance().getInputHandler();
    this.fileWriter = GlobalProtocol.getInstance().getFileWriter();
    this.image = new BufferedImage(PLAYGROUND_WIDTH, PLAYGROUND_HEIGHT, BufferedImage.TYPE_INT_RGB);
    this.graphics = this.image.createGraphics();
    this.imagePositionMap = new LinkedHashMap<>();
    this.imageDrawOrderStack = new ArrayList<>();
    this.audioWriter = new AudioWriter.Factory().createAudioWriter(new ByteArrayOutputStream());

    this.isRunning = false;
    this.updateRequested = false;
    this.exitRequested = false;
  }

  /**
   * Returns the width of the playground screen. This will always be 400.
   *
   * @return the width of the playground in pixels.
   */
  public int getWidth() {
    return PLAYGROUND_WIDTH;
  }

  /**
   * Returns the height of the playground screen. This will always be 400.
   *
   * @return the height of the playground in pixels.
   */
  public int getHeight() {
    return PLAYGROUND_HEIGHT;
  }

  /**
   * Sets the background of the playground to the provided image. The image will be scaled to fit
   * the full playground screen, which may distory the image if it is not square.
   *
   * @param filename the name of the file from the asset manager to put in the background
   * @throws FileNotFoundException if the file cannot be found in the asset manager
   */
  public void setBackgroundImage(String filename) throws FileNotFoundException {
    this.backgroundImage = new Image(filename);
    this.updateRequested = true;
  }

  /**
   * Adds a clickable image from the playground.
   *
   * @param image the image to add. If the image is already in the playground, this method does
   *     nothing.
   */
  public void addClickableImage(ClickableImage image, int x, int y, int width, int height) {
    this.addImage(image, x, y, width, height);
  }

  /**
   * Removes the clickable image from the playground.
   *
   * @param image the image to remove. If the image is not in the playground, this method does
   *     nothing.
   */
  public void removeClickableImage(ClickableImage image) {
    this.removeImage(image);
  }

  /**
   * Adds a non-clickable image from the playground.
   *
   * @param image the image to add. If the image is already in the playground, this method does
   *     nothing.
   */
  public void addImage(Image image, int x, int y, int width, int height) {
    if (!this.imagePositionMap.containsKey(image)) {
      this.imagePositionMap.put(image, new ImagePosition(x, y, width, height));
      this.imageDrawOrderStack.add(image);
      this.updateRequested = true;
    }
  }

  /**
   * Removes the image from the playground.
   *
   * @param image the image to remove. If the image is not in the playground, this method does
   *     nothing.
   */
  public void removeImage(Image image) {
    if (this.imagePositionMap.containsKey(image)) {
      this.imagePositionMap.remove(image);
      this.imageDrawOrderStack.remove(image);
      this.updateRequested = true;
    }
  }

  /**
   * Starts the playground game, waiting for the user to click on images and executing the
   * appropriate code. To end the game, call the end() method. The run() method may only be called
   * once per execution of a program.
   *
   * @throws PlaygroundRunningException if the run() method has already been called.
   */
  public void run() {
    if (this.isRunning) {
      // TODO throw exception
      return;
    }
    this.outputAdapter.sendMessage(new PlaygroundMessage(PlaygroundSignalKey.RUN, new HashMap<>()));
    this.isRunning = true;

    // Dispatch initial state if update is pending
    if (this.updateRequested) {
      this.dispatchPlaygroundUpdate();
    }

    // Wait for next user input
    while (this.isRunning) {
      final JSONObject message =
          new JSONObject(this.inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND));
      this.onCoordinateClicked(message.getInt("x"), message.getInt("y"));

      // Dispatch new playground image and sound if changes were made
      if (this.updateRequested) {
        this.dispatchPlaygroundUpdate();
        this.updateRequested = false;
      }

      // If exit was called in a callback, dispatch an exit message ending the game
      if (this.exitRequested) {
        this.dispatchExitMessage();
        this.exitRequested = false;
        this.isRunning = false;
      }
    }
  }

  /**
   * Ends the game, plays the sound supplied, and stops program execution.
   *
   * @param endingSound the name of a sound file in the asset manager to play at the end of the
   *     game.
   * @throws PlaygroundNotRunningException if the run() method has not been called.
   * @throws FileNotFoundException if the sound file cannot be found.
   */
  public void exit(String endingSound) throws FileNotFoundException {
    if (!this.isRunning) {
      // TODO throw exception
      return;
    }
    this.exitSound = endingSound;
    this.exitRequested = true;
  }

  /**
   * Ends the game and stops program execution.
   *
   * @throws PlaygroundNotRunningException if the run() method has not been called.
   */
  public void exit() {
    if (!this.isRunning) {
      // TODO throw exception
      return;
    }
    this.exitRequested = true;
  }

  private void onCoordinateClicked(int x, int y) {
    System.out.printf("Coordinate clicked: (%d, %d)", x, y);
    // Find first clickable image to handle click, in order of last drawn image
    for (int i = this.imageDrawOrderStack.size() - 1; i >= 0; i--) {
      final Image image = this.imageDrawOrderStack.get(i);
      if (image instanceof ClickableImage
          && this.imagePositionMap.get(image).containsCoordinates(x, y)) {
        final ClickableImage clickableImage = (ClickableImage) image;
        clickableImage.onClick();
        if (clickableImage.getClickSound() != null) {
          this.soundFilename = clickableImage.getClickSound();
        } else {
          this.soundFilename = null;
        }
        break;
      }
    }
  }

  private void dispatchPlaygroundUpdate() {
    final HashMap<String, String> details = new HashMap<>();
    details.put("imageUrl", this.writePlaygroundImageToUrl());

    if (this.soundFilename != null) {
      try {
        details.put("audioUrl", this.writeAudioFileToUrl(this.soundFilename));
      } catch (FileNotFoundException e) {
        // TODO exception handling
        System.out.println(e);
      }
    }

    this.outputAdapter.sendMessage(new PlaygroundMessage(PlaygroundSignalKey.UPDATE, details));
    this.graphics.clearRect(0, 0, this.getWidth(), this.getHeight());
  }

  private void dispatchExitMessage() {
    final HashMap<String, String> details = new HashMap<>();
    if (this.exitSound != null) {
      try {
        details.put("audioUrl", this.writeAudioFileToUrl(this.exitSound));
      } catch (FileNotFoundException e) {
        // TODO handle / refactor to bubble up exception
        System.out.println(e);
      }
    }
    this.outputAdapter.sendMessage(new PlaygroundMessage(PlaygroundSignalKey.EXIT, details));
  }

  private String writePlaygroundImageToUrl() {
    // Draw images in order, starting with background image
    if (this.backgroundImage != null) {
      this.graphics.drawImage(
          this.backgroundImage.getBufferedImage(), 0, 0, PLAYGROUND_WIDTH, PLAYGROUND_HEIGHT, null);
    }
    for (Image image : this.imageDrawOrderStack) {
      final ImagePosition position = this.imagePositionMap.get(image);
      this.graphics.drawImage(
          image.getBufferedImage(), position.x, position.y, position.width, position.height, null);
    }

    final ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream();
    try {
      ImageIO.write(this.image, OUTPUT_IMAGE_FILE_FORMAT, imageOutputStream);
      return this.generateOutputUrl(
          imageOutputStream, PLAYGROUND_IMAGE_FILE_NAME, OUTPUT_IMAGE_CONTENT_TYPE);
    } catch (IOException e) {
      throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }

  private String writeAudioFileToUrl(String audioFilename) throws FileNotFoundException {
    final ByteArrayOutputStream audioOutputStream = new ByteArrayOutputStream();

    this.audioWriter.writeAudioFromAssetFile(audioFilename);
    this.audioWriter.writeToAudioStreamAndClose(audioOutputStream);
    this.audioWriter.reset();

    return this.generateOutputUrl(
        audioOutputStream, PLAYGROUND_AUDIO_FILE_NAME, OUTPUT_AUDIO_CONTENT_TYPE);
  }

  private String generateOutputUrl(
      ByteArrayOutputStream outputStream, String filename, String contentType) {
    try {
      return this.fileWriter.writeToFile(filename, outputStream.toByteArray(), contentType);
    } catch (JavabuilderException e) {
      throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }
}
