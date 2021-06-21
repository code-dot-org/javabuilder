package org.code.media;

public class Color {

    /**
     * Creates a color from a string representation.
     * 
     * @param color the string name of the color
     * @throws IllegalArgumentException if the value specifies an unsupported color
     *                                  name or illegal hexadecimal value
     */
    public Color(String color) throws IllegalArgumentException {

    }

    /**
     * Create a new color based on the red, green, and blue values provided.
     * 
     * @param red   the red value from 0 - 255
     * @param green the green value from 0 - 255
     * @param blue  the blue value from 0 - 255
     */
    public Color(int red, int green, int blue) {

    }

    /**
     * Returns the amount of red (ranging from 0 to 255).
     * 
     * @return a number representing the red value (between 0 and 255)
     */
    public int getRed() {
        return -1;
    }

    /**
     * Returns the amount of green (ranging from 0 to 255).
     * 
     * @return a number representing the green value (between 0 and 255) of the
     *         pixel.
     */
    public int getGreen() {
        return -1;
    }

    /**
     * Returns the amount of blue (ranging from 0 to 255).
     * 
     * @return a number representing the blue value (between 0 and 255)
     */
    public int getBlue() {
        return -1;
    }

    /**
     * Sets the amount of red (ranging from 0 to 255). Values below 0 will be
     * ignored and set to 0, and values above 255 with be ignored and set to 255.
     * 
     * @param value the amount of red (ranging from 0 to 255) in the color of the
     *              pixel.
     */
    public void setRed(int value) {
    }

    /**
     * Sets the amount of green (ranging from 0 to 255). Values below 0 will be
     * ignored and set to 0, and values above 255 with be ignored and set to 255.
     * 
     * @param value the amount of green (ranging from 0 to 255) in the color of the
     *              pixel.
     */
    public void setGreen(int value) {
    }

    /**
     * Sets the amount of blue (ranging from 0 to 255).
     * 
     * @param value the amount of blue (ranging from 0 to 255) in the color of the
     *              pixel.
     */
    public void setBlue(int value) {
    }

    public static final Color white = new Color(255, 255, 255);
    public static final Color silver = new Color(192, 192, 192);
    public static final Color gray = new Color(128, 128, 128);
    public static final Color black = new Color(0, 0, 0);
    public static final Color red = new Color(255, 0, 0);
    public static final Color maroon = new Color(128, 0, 0);
    public static final Color yellow = new Color(256, 256, 0);
    public static final Color olive = new Color(128, 128, 0);
    public static final Color lime = new Color(0, 256, 0);
    public static final Color green = new Color(0, 128, 0);
    public static final Color aqua = new Color(0, 255, 255);
    public static final Color teal = new Color(0, 128, 128);
    public static final Color blue = new Color(0, 0, 255);
    public static final Color navy = new Color(0, 0, 128);
    public static final Color fuchsia = new Color(255, 0, 255);
    public static final Color purple = new Color(128, 0, 128);

    public static final Color pink = new Color(255, 192, 203);
    public static final Color orange = new Color(255, 165, 0);
    public static final Color gold = new Color(255, 215, 0);
    public static final Color brown = new Color(165, 42, 42);
    public static final Color chocolate = new Color(210, 105, 30);
    public static final Color tan = new Color(210, 180, 140);
    public static final Color turquoise = new Color(64, 224, 208);
    public static final Color indigo = new Color(75, 0, 130);
    public static final Color violet = new Color(238, 130, 238);
    public static final Color beige = new Color(245, 245, 220);
    public static final Color ivory = new Color(255, 255, 240);
}
