package org.code.media;

public class Pixel {
  private final Image image;
  private final int x;
  private final int y;
  private Color color;

  /**
   * Protected constructor as this will only be called within Image
   *
   * @param image
   * @param x
   * @param y
   * @param color
   */
  protected Pixel(Image image, int x, int y, Color color) {
    this.image = image;
    this.x = x;
    this.y = y;
    this.color = color;
  }

  /**
   * Get the X position of this pixel in the image
   *
   * @return the x position of the pixel
   */
  public int getX() {
    return this.x;
  }

  /**
   * Get the Y position of this pixel in the image
   *
   * @return the y position of the pixel
   */
  public int getY() {
    return this.y;
  }

  /**
   * Get the image that this pixel is part of
   *
   * @return the image that this pixel part of
   */
  public Image getSourceImage() {
    return this.image;
  }

  /** */

  /**
   * Get the color of the pixel in the image
   *
   * @return
   */
  public Color getColor() {
    return this.color;
  }

  /**
   * Set the color of the pixel
   *
   * @param color the color to set the pixel
   */
  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Returns the amount of red (ranging from 0 to 255) in the color of the pixel.
   *
   * @return a number representing the red value (between 0 and 255) of the pixel.
   */
  public int getRed() {
    return this.color.getRed();
  }

  /**
   * Returns the amount of green (ranging from 0 to 255) in the color of the pixel.
   *
   * @return a number representing the green value (between 0 and 255) of the pixel.
   */
  public int getGreen() {
    return this.color.getGreen();
  }

  /**
   * Returns the amount of blue (ranging from 0 to 255) in the color of the pixel. Values below 0
   * will be ignored and set to 0, and values above 255 with be ignored and set to 255.
   *
   * @return a number representing the blue value (between 0 and 255) of the pixel.
   */
  public int getBlue() {
    return this.color.getBlue();
  }

  /**
   * Sets the amount of red (ranging from 0 to 255) in the color of the pixel. Values below 0 will
   * be ignored and set to 0, and values above 255 with be ignored and set to 255.
   *
   * @param value the amount of red (ranging from 0 to 255) in the color of the pixel.
   */
  public void setRed(int value) {
    this.color.setRed(value);
  }

  /**
   * Sets the amount of green (ranging from 0 to 255) in the color of the pixel. Values below 0 will
   * be ignored and set to 0, and values above 255 with be ignored and set to 255.
   *
   * @param value the amount of green (ranging from 0 to 255) in the color of the pixel.
   */
  public void setGreen(int value) {
    this.color.setGreen(value);
  }

  /**
   * Sets the amount of blue (ranging from 0 to 255) in the color of the pixel.
   *
   * @param value the amount of blue (ranging from 0 to 255) in the color of the pixel.
   */
  public void setBlue(int value) {
    this.color.setBlue(value);
  }
}
