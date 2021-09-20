package org.code.playground;

import org.code.media.Color;
import org.code.media.Font;
import org.code.media.FontStyle;

public class TextItem extends Item {
  private String text;
  private Color color;
  private Font font;
  private FontStyle fontStyle;
  private double rotation;

  /**
   * Creates text item that can be placed the board.
   *
   * @param text the text content
   * @param x the distance from the left side of the board
   * @param y the distance from the top of the board
   * @param color the color to draw the text
   * @param font the name of the font to draw the text
   * @param fontStyle the style of the font
   * @param height the height of the text in pixels
   * @param rotation the rotation or tilt of the text, in degrees
   */
  public TextItem(
      String text,
      int x,
      int y,
      Color color,
      Font font,
      FontStyle fontStyle,
      int height,
      double rotation) {
    super(x, y, height);
    this.text = text;
    this.color = color;
    this.font = font;
    this.fontStyle = fontStyle;
    this.rotation = rotation;
  }

  /**
   * Creates text item that can be placed the board in a normal font style.
   *
   * @param text the text content
   * @param x the distance from the left side of the board
   * @param y the distance from the top of the board
   * @param color the color to draw the text
   * @param font the name of the font to draw the text
   * @param height the height of the text in pixels
   * @param rotation the rotation or tilt of the text, in degrees.
   */
  public TextItem(String text, int x, int y, Color color, Font font, int height, double rotation) {
    super(x, y, height);
    this.text = text;
    this.color = color;
    this.font = font;
    this.rotation = rotation;
  }

  /**
   * Set the text content for the item.
   *
   * @param text the content for the item
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Get the text content for the item.
   *
   * @return text the content for the item
   */
  public String getText() {
    return this.text;
  }

  /**
   * Set the text color for the item.
   *
   * @param color the text color
   */
  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Get the text color for the item.
   *
   * @return the text color for the item
   */
  public Color getColor() {
    return this.color;
  }

  /**
   * Set the font for the text.
   *
   * @param font the font for the text.
   */
  public void setFont(Font font) {
    this.font = font;
  }

  /**
   * Get the font for the text.
   *
   * @return the font for the text.
   */
  public Font getFont() {
    return this.font;
  }

  /**
   * Set the font style for the text.
   *
   * @param fontStyle the font style for the item
   */
  public void setFontStyle(FontStyle fontStyle) {
    this.fontStyle = fontStyle;
  }

  /**
   * Get the font style for the text.
   *
   * @return the font style for the item
   */
  public FontStyle getFontStyle() {
    return this.fontStyle;
  }

  /**
   * Set the rotation for the text.
   *
   * @param rotation the rotation for the text, in degrees
   */
  public void setRotation(double rotation) {
    this.rotation = rotation;
  }

  /**
   * Get the rotation for the text.
   *
   * @return the rotation for the text, in degrees
   */
  public double getRotation() {
    return this.rotation;
  }
}
