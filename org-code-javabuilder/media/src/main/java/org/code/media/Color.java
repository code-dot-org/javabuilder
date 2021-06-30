package org.code.media;

public class Color {

  private int red;
  private int green;
  private int blue;

  /**
   * Creates a color from a string representation.
   *
   * @param color the string name of the color
   * @throws IllegalArgumentException if the value specifies an unsupported color name or illegal
   *     hexadecimal value
   */
  public Color(String color) throws IllegalArgumentException {
    // TODO: Implement this method
  }

  /**
   * Create a new color based on the red, green, and blue values provided.
   *
   * @param red the red value from 0 - 255
   * @param green the green value from 0 - 255
   * @param blue the blue value from 0 - 255
   */
  public Color(int red, int green, int blue) {
    this.red = this.sanitizeValue(red);
    this.green = this.sanitizeValue(green);
    this.blue = this.sanitizeValue(blue);
  }

  /**
   * Initialize a color with the combined RGB integer value consisting of the red component in bits
   * 16-23, the green component in bits 8-15, and the blue component in bits 0-7. Only used for
   * conversion between BufferedImage and org.code.media.Image
   *
   * @param rgb
   */
  protected Color(int rgb) {
    this.red = (rgb >> 16) & 255;
    this.blue = rgb & 255;
    this.green = (rgb >> 8) & 255;
  }

  /**
   * Returns the amount of red (ranging from 0 to 255).
   *
   * @return a number representing the red value (between 0 and 255)
   */
  public int getRed() {
    return this.red;
  }

  /**
   * Returns the amount of green (ranging from 0 to 255).
   *
   * @return a number representing the green value (between 0 and 255) of the pixel.
   */
  public int getGreen() {
    return this.green;
  }

  /**
   * Returns the amount of blue (ranging from 0 to 255).
   *
   * @return a number representing the blue value (between 0 and 255)
   */
  public int getBlue() {
    return this.blue;
  }

  /**
   * Sets the amount of red (ranging from 0 to 255). Values below 0 will be ignored and set to 0,
   * and values above 255 will be ignored and set to 255.
   *
   * @param value the amount of red (ranging from 0 to 255) in the color of the pixel.
   */
  public void setRed(int value) {
    this.red = this.sanitizeValue(value);
  }

  /**
   * Sets the amount of green (ranging from 0 to 255). Values below 0 will be ignored and set to 0,
   * and values above 255 will be ignored and set to 255.
   *
   * @param value the amount of green (ranging from 0 to 255) in the color of the pixel.
   */
  public void setGreen(int value) {
    this.green = this.sanitizeValue(value);
  }

  /**
   * Sets the amount of blue (ranging from 0 to 255). Values below 0 will be ignored and set to 0, *
   * and values above 255 will be ignored and set to 255.
   *
   * @param value the amount of blue (ranging from 0 to 255) in the color of the pixel.
   */
  public void setBlue(int value) {
    this.blue = this.sanitizeValue(value);
  }

  /**
   * @return the combined RGB integer value consisting of the red component in bits 16-23, the green
   *     component in bits 8-15, and the blue component in bits 0-7.
   */
  protected int getRGB() {
    return (this.red << 16 | this.green << 8 | this.blue);
  }

  /**
   * Values below 0 will be set to 0, and values above 255 will be set to 255.
   *
   * @param value
   * @return value if it was in the expected range, or a valid value based on the reset logic.
   */
  private int sanitizeValue(int value) {
    if (value < 0) {
      return 0;
    }
    if (value > 255) {
      return 255;
    }
    return value;
  }

  public static java.awt.Color convertToAWTColor(Color c) {
    return new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue());
  }

  public static final Color WHITE = new Color(255, 255, 255);
  public static final Color SILVER = new Color(192, 192, 192);
  public static final Color GRAY = new Color(128, 128, 128);
  public static final Color BLACK = new Color(0, 0, 0);
  public static final Color RED = new Color(255, 0, 0);
  public static final Color MAROON = new Color(128, 0, 0);
  public static final Color YELLOW = new Color(255, 255, 0);
  public static final Color OLIVE = new Color(128, 128, 0);
  public static final Color LIME = new Color(0, 256, 0);
  public static final Color GREEN = new Color(0, 128, 0);
  public static final Color AQUA = new Color(0, 255, 255);
  public static final Color TEAL = new Color(0, 128, 128);
  public static final Color BLUE = new Color(0, 0, 255);
  public static final Color NAVY = new Color(0, 0, 128);
  public static final Color FUCHSIA = new Color(255, 0, 255);
  public static final Color PURPLE = new Color(128, 0, 128);
  public static final Color PINK = new Color(255, 192, 203);
  public static final Color ORANGE = new Color(255, 165, 0);
  public static final Color GOLD = new Color(255, 215, 0);
  public static final Color BROWN = new Color(165, 42, 42);
  public static final Color CHOCOLATE = new Color(210, 105, 30);
  public static final Color TAN = new Color(210, 180, 140);
  public static final Color TURQUOISE = new Color(64, 224, 208);
  public static final Color INDIGO = new Color(75, 0, 130);
  public static final Color VIOLET = new Color(238, 130, 238);
  public static final Color BEIGE = new Color(245, 245, 220);
  public static final Color IVORY = new Color(255, 255, 240);
}
