package org.code.media;

public class Pixel {
    /**
     * Private constructor as this will only be used when a getPixel or getPixels
     * method is called on an image class
     * 
     * @param image
     * @param x
     * @param y
     */
    private Pixel(Image image, int x, int y) {
    }

    /**
     * Get the X position of this pixel in the image
     * 
     * @return the x position of the pixel
     */
    public int getX() {
        return -1;
    }

    /**
     * Get the Y position of this pixel in the image
     * 
     * @return the y position of the pixel
     */
    public int getY() {
        return -1;
    }

    /**
     * Get the image that this pixel is part of
     * 
     * @return the image that this pixel part of
     */
    public Image getSourceImage() {
        return null;
    }

    /** */

    /**
     * Get the color of the pixel in the image
     * 
     * @return
     */
    public Color getColor() {
        return null;
    }

    /**
     * Set the color of the pixel
     * 
     * @param color the color to set the pixel
     */
    public void setColor(Color color) {

    }

    /**
     * Returns the amount of red (ranging from 0 to 255) in the color of the pixel.
     * 
     * @return a number representing the red value (between 0 and 255) of the pixel.
     */
    public int getRed() {
        return -1;
    }

    /**
     * Returns the amount of green (ranging from 0 to 255) in the color of the
     * pixel.
     * 
     * @return a number representing the green value (between 0 and 255) of the
     *         pixel.
     */
    public int getGreen() {
        return -1;
    }

    /**
     * Returns the amount of blue (ranging from 0 to 255) in the color of the pixel.
     * Values below 0 will be ignored and set to 0, and values above 255 with be
     * ignored and set to 255.
     * 
     * @return a number representing the blue value (between 0 and 255) of the
     *         pixel.
     */
    public int getBlue() {
        return -1;
    }

    /**
     * Sets the amount of red (ranging from 0 to 255) in the color of the pixel.
     * Values below 0 will be ignored and set to 0, and values above 255 with be
     * ignored and set to 255.
     * 
     * @param value the amount of red (ranging from 0 to 255) in the color of the
     *              pixel.
     */
    public void setRed(int value) {
    }

    /**
     * Sets the amount of green (ranging from 0 to 255) in the color of the pixel.
     * Values below 0 will be ignored and set to 0, and values above 255 with be
     * ignored and set to 255.
     * 
     * @param value the amount of green (ranging from 0 to 255) in the color of the
     *              pixel.
     */
    public void setGreen(int value) {
    }

    /**
     * Sets the amount of blue (ranging from 0 to 255) in the color of the pixel.
     * 
     * @param value the amount of blue (ranging from 0 to 255) in the color of the
     *              pixel.
     */
    public void setBlue(int value) {
    }
}
