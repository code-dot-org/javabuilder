package org.code.playground;

import java.io.FileNotFoundException;

public final static class Playground {
    /**
     * Returns the width of the playground screen. This will always be 400.
     * 
     * @return the width of the playground in pixels.
     */
    public static int getWidth();

    /**
     * Returns the height of the playground screen. This will always be 400.
     * 
     * @return the height of the playground in pixels.
     */
    public static int getHeight();

    /**
     * Sets the background of the playground to the provided image. The image will
     * be scaled to fit the full playground screen, which may distory the image if
     * it is not square.
     * 
     * @param filename the name of the file from the asset manager to put in the
     *                 background
     * @throws FileNotFoundException if the file cannot be found in the asset
     *                               manager
     */
    public static void setBackgroundImage(String filename) throws FileNotFoundException;

    /**
     * Adds a clickable image from the playground.
     * 
     * @param image the image to add. If the image is already in the playground,
     *              this method does nothing.
     */
    public static void addClickableImage(ClickableImage image);

    /**
     * Removes the clickable image from the playground.
     * 
     * @param image the image to remove. If the image is not in the playground, this
     *              method does nothing.
     */
    public static void removeClickableImage(ClickableImage img);

    /**
     * Adds a non-clickable image from the playground.
     * 
     * @param image the image to add. If the image is already in the playground,
     *              this method does nothing.
     */
    public static void addImage(Image image);

    /**
     * Removes the image from the playground.
     * 
     * @param image the image to remove. If the image is not in the playground, this
     *              method does nothing.
     */
    public static void removeImage(Image image);

    /**
     * Starts the playground game, waiting for the user to click on images and
     * executing the appropriate code. To end the game, call the end() method. The
     * run() method may only be called once per execution of a program.
     * 
     * @throws PlaygroundRunningException if the run() method has already been
     *                                    called.
     */
    public static void run() throws PlaygroundRunningException;

    /**
     * Ends the game, plays the sound supplied, and stops program execution.
     * @param endingSound the name of a sound file in the asset manager to play at the end of the game.
     * @throws PlaygroundNotRunningException if the run() method has not been
     *                                       called.
     * @throws FileNotFoundException if the sound file cannot be found.
     */
    public static void exit(String endingSound) throws PlaygroundNotRunningException, FileNotFoundException;

    /**
     * Ends the game and stops program execution.
     * 
     * @throws PlaygroundNotRunningException if the run() method has not been
     *                                       called.
     */
    public static void exit();
}
