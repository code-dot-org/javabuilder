package org.code.playground;

import java.io.FileNotFoundException;

public class Board {
    /**
     * Returns the width of the board. This will always be 400.
     * 
     * @return the width of the board in pixels.
     */
    public int getWidth() { return -1; }

    /**
     * Returns the height of the board. This will always be 400.
     * 
     * @return the height of the board in pixels.
     */
    public int getHeight() { return -1; }

    /**
     * Sets the background of the board to the provided image. The image will
     * be scaled to fit the full board, which may distort the image if
     * it is not square.
     * 
     * @param filename the name of the file from the asset manager to put in the
     *                 background
     * @throws FileNotFoundException if the file cannot be found in the asset
     *                               manager
     */
    public void setBackgroundImage(String filename) throws FileNotFoundException {}

    /**
     * Adds a clickable image to the board.
     * 
     * @param image the image to add. If the image is already on the board,
     *              this method does nothing.
     */
    public void addClickableImage(ClickableImage image) {}

    /**
     * Removes the clickable image from the board.
     * 
     * @param image the image to remove. If the image is not on the board, this
     *             method does nothing.
     */
    public void removeClickableImage(ClickableImage image) {}

    /**
     * Adds a non-clickable item to the board. 
     *
     * @param item the item to add. If the item is already on the board,
     *             this method does nothing.
     */
    public void addItem(Item item) {}

    /**
     * Removes the non-clickable item from the board.
     * 
     * @param item the item to remove. If the image is not on the board, this
     *             method does nothing.
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
     * Starts the playground game, waiting for the user to click on images and
     * executing the appropriate code. To end the game, call the end() method. The
     * run() method may only be called once per execution of a program.
     * 
     * @throws PlaygroundException if the run() method has already been
     *                                    called.
     */
    public void run() throws PlaygroundException {}

    /**
     * Ends the game, plays the sound supplied, and stops program execution.
     * @param endingSound the name of a sound file in the asset manager to play at the end of the game.
     * @throws PlaygroundException if the run() method has not been
     *                                       called.
     * @throws FileNotFoundException if the sound file cannot be found.
     */
    public void exit(String endingSound) throws PlaygroundException, FileNotFoundException {}

    /**
     * Ends the game and stops program execution.
     * 
     * @throws PlaygroundException if the run() method has not been
     *                                       called.
     */
    public void exit() {}
}
