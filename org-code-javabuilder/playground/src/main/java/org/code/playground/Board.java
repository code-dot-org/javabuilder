package org.code.playground;

import java.io.FileNotFoundException;
import java.util.HashMap;
import org.code.protocol.ClientMessageDetailKeys;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.InputHandler;
import org.code.protocol.InputMessageType;

public class Board {

  private static final int BOARD_WIDTH = 400;
  private static final int BOARD_HEIGHT = 400;

  private final PlaygroundMessageHandler playgroundMessageHandler;
  private final InputHandler inputHandler;

  private boolean firstRunStarted;
  private boolean isRunning;

  protected Board() {
    this(PlaygroundMessageHandler.getInstance(), GlobalProtocol.getInstance().getInputHandler());
  }

  Board(PlaygroundMessageHandler playgroundMessageHandler, InputHandler inputHandler) {
    this.playgroundMessageHandler = playgroundMessageHandler;
    this.inputHandler = inputHandler;
    this.firstRunStarted = false;
    this.isRunning = false;
  }

  /**
   * Returns the width of the board. This will always be 400.
   *
   * @return the width of the board in pixels.
   */
  public int getWidth() {
    return BOARD_WIDTH;
  }

  /**
   * Returns the height of the board. This will always be 400.
   *
   * @return the height of the board in pixels.
   */
  public int getHeight() {
    return BOARD_HEIGHT;
  }

  /**
   * Sets the background of the board to the provided image. The image will be scaled to fit the
   * full board, which may distort the image if it is not square.
   *
   * @param filename the name of the file from the asset manager to put in the background
   * @throws FileNotFoundException if the file cannot be found in the asset manager
   */
  public void setBackgroundImage(String filename) throws FileNotFoundException {
    final HashMap<String, String> details = new HashMap<>();
    details.put(ClientMessageDetailKeys.FILENAME.toString(), filename);

    this.playgroundMessageHandler.sendMessage(
        new PlaygroundMessage(PlaygroundSignalKey.SET_BACKGROUND_IMAGE, details));
  }

  /**
   * Adds a clickable image to the board.
   *
   * @param image the image to add. If the image is already on the board, this method does nothing.
   */
  public void addClickableImage(ClickableImage image) {}

  /**
   * Removes the clickable image from the board.
   *
   * @param image the image to remove. If the image is not on the board, this method does nothing.
   */
  public void removeClickableImage(ClickableImage image) {}

  /**
   * Adds a non-clickable item to the board.
   *
   * @param item the item to add. If the item is already on the board, this method does nothing.
   */
  public void addItem(Item item) {}

  /**
   * Removes the non-clickable item from the board.
   *
   * @param item the item to remove. If the image is not on the board, this method does nothing.
   */
  public void removeItem(Item item) {}

  /**
   * Plays a sound from the asset manager.
   *
   * @param filename the name of the sound file from the asset manager to play
   * @throws FileNotFoundException when the sound file cannot be found.
   */
  public void playSound(String filename) throws FileNotFoundException {}

  /**
   * Starts the playground game, waiting for the user to click on images and executing the
   * appropriate code. To end the game, call the exit() method. The run() method may only be called
   * once per execution of a program.
   *
   * @throws PlaygroundException if the run() method has already been called.
   */
  public void run() throws PlaygroundException {
    if (this.firstRunStarted) {
      throw new PlaygroundException(PlaygroundExceptionKeys.PLAYGROUND_RUNNING);
    }

    this.playgroundMessageHandler.sendMessage(
        new PlaygroundMessage(PlaygroundSignalKey.RUN, new HashMap<>()));

    this.firstRunStarted = true;
    this.isRunning = true;

    // Keep waiting for user input while game is running
    while (this.isRunning) {
      final String message = this.inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND);
      if (message != null) {
        this.handleClickEvent(message);
      }
    }
  }

  /**
   * Ends the game, plays the sound supplied, and stops program execution.
   *
   * @param endingSound the name of a sound file in the asset manager to play at the end of the
   *     game.
   * @throws PlaygroundException if the run() method has not been called.
   * @throws FileNotFoundException if the sound file cannot be found.
   */
  public void exit(String endingSound) throws PlaygroundException, FileNotFoundException {}

  /**
   * Ends the game and stops program execution.
   *
   * @throws PlaygroundException if the run() method has not been called.
   */
  public void exit() throws PlaygroundException {
    if (!this.isRunning) {
      throw new PlaygroundException(PlaygroundExceptionKeys.PLAYGROUND_NOT_RUNNING);
    }

    this.playgroundMessageHandler.sendMessage(
        new PlaygroundMessage(PlaygroundSignalKey.EXIT, new HashMap<>()));
    this.isRunning = false;
  }

  private void handleClickEvent(String id) {
    // TODO: Handle updates
  }
}
