package org.code.playground;

import java.io.FileNotFoundException;
import org.code.media.Color;
import org.code.media.Font;
import org.code.media.FontStyle;

public class Board {
    /**
     * Returns the width of the board. This will always be 400.
     * 
     * @return the width of the board in pixels.
     */
    public int getWidth();

    /**
     * Returns the height of the board. This will always be 400.
     * 
     * @return the height of the board in pixels.
     */
    public int getHeight();

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
    public void setBackgroundImage(String filename) throws FileNotFoundException;

    /**
     * Adds a clickable item to the board.
     * 
     * @param item the item to add. If the item is already on the board,
     *             this method does nothing.
     */
    public void addClickableItem(ClickableItem item);

    /**
     * Removes the clickable item from the board.
     * 
     * @param item the item to remove. If the item is not on the board, this
     *             method does nothing.
     */
    public void removeClickableItem(ClickableItem item);

    /**
     * Adds a non-clickable item to the board. 
     *
     * @param item the item to add. If the item is already on the board,
     *             this method does nothing.
     */
    public void addItem(Item item, int x, int y, int width, int height);

    /**
     * Removes the non-clickable item from the board.
     * 
     * @param item the item to remove. If the image is not on the board, this
     *             method does nothing.
     */
    public void removeItem(Item item);
    
    /**
     * Draws text on the board.
     *
     * @param text the text to draw
     * @param x the distance from the left side of the board to draw the text.
     * @param y the distance from the top of the board to draw the text.
     * @param color the color to draw the text.
     * @param font the name of the font to draw the text in.
     * @param fontStyle the name of the font style to draw the text in.
     * @param height the height of the text in pixels.
     * @param rotation the rotation or tilt of the text, in degrees
     */
    public drawText(String text, int x, int y, Color color, Font font, FontStyle fontStyle, int height, double rotation);

    /**
     * Draws text on the board with a normal font style
     *
     * @param text the text to draw
     * @param x the distance from the left side of the board to draw the text.
     * @param y the distance from the top of the board to draw the text.
     * @param color the color to draw the text.
     * @param font the name of the font to draw the text in.
     * @param height the height of the text in pixels.
     * @param rotation the rotation or tilt of the text, in degrees.
     */
    public drawText(String text, int x, int y, Color color, Font font, int height, double rotation);

    /**
     * Starts the playground game, waiting for the user to click on images and
     * executing the appropriate code. To end the game, call the end() method. The
     * run() method may only be called once per execution of a program.
     * 
     * @throws PlaygroundRunningException if the run() method has already been
     *                                    called.
     */
    public void run() throws PlaygroundRunningException;

    /**
     * Ends the game, plays the sound supplied, and stops program execution.
     * @param endingSound the name of a sound file in the asset manager to play at the end of the game.
     * @throws PlaygroundNotRunningException if the run() method has not been
     *                                       called.
     * @throws FileNotFoundException if the sound file cannot be found.
     */
    public void exit(String endingSound) throws PlaygroundNotRunningException, FileNotFoundException;

    /**
     * Ends the game and stops program execution.
     * 
     * @throws PlaygroundNotRunningException if the run() method has not been
     *                                       called.
     */
    public void exit();
}
