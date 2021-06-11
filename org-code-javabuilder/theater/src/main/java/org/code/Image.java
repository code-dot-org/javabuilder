package org.code;

public class Image {
    /**
     * Creates a new image object, using the pixel information from the file
     * uploaded to the asset manager.
     * 
     * @param filename the name of the image loaded into the asset manager for the
     *                 project
     * @throws FileNotFoundException if the file doesn't exist in the asset manager.
     */
    public Image(String filename) throws FileNotFoundException;

    /**
     * Create a new image object from the pixels provided. If the number of pixels
     * is not equal to the width multiplied by the height, the remaining pixels will
     * be filled with black. If there are too many pixels, the remaining pixels will
     * be cut off in the image.
     * 
     * @param pixels the pixels with which to create the image.
     * @param width  the width of the image to create.
     * @param height the height of the image to create.
     */
    public Image(Pixel[] pixels, int width, int height) {
    }

    /**
     * Creates an empty image filled with white pixels.
     * 
     * @param width  the width of the image to create.
     * @param height the height of the image to create.
     */
    public Image(int width, int height) {
    }

    /**
     * Get an array with all of the pixels of the image.
     * 
     * @return the pixels in the image. This array will have a length equals to the
     *         width multiplied by the height.
     */
    public Pixel[] getPixels() {
    }

    /**
     * Gets the width of the image in pixels.
     * 
     * @return the width of the image in pixels.
     */
    public int getWidth() {
    }

    /**
     * Gets the height of the image in pixels.
     * 
     * @return the height of the image in pixels.
     */
    public int getHeight() {
    }

    /**
     * Clears the image, filling it with the color provided.
     * 
     * @param color the color with which to fill the image.
     */
    public void clear(String color) {
    }
}
