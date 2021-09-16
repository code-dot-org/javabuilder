package org.code.playground;

import org.code.media.Color;
import org.code.media.Font;
import org.code.media.FontStyle;

public class TextItem implements Item {
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
      double rotation) {}

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
  public TextItem(String text, int x, int y, Color color, Font font, int height, double rotation) {}

  /**
   * Set the text content for the item.
   *
   * @param text the content for the item
   */
  public void setText(String text) {}

  /**
   * Get the text content for the item.
   *
   * @return text the content for the item
   */
  public String getText() {
    return "text";
  }

  /**
   * Set the text color for the item.
   *
   * @param color the text color
   */
  public void setColor(Color color) {}

  /**
   * Get the text color for the item.
   *
   * @return the text color for the item
   */
  public Color getColor() {
    return Color.BLACK;
  }

  /**
   * Set the font for the text.
   *
   * @param font the font for the text.
   */
  public void setFont(Font font) {}

  /**
   * Get the font for the text.
   *
   * @return the font for the text.
   */
  public Font getFont() {
    return Font.MONO;
  }

  /**
   * Set the font style for the text.
   *
   * @param fontStyle the font style for the item
   */
  public void setFontStyle(FontStyle fontStyle) {}

  /**
   * Get the font style for the text.
   *
   * @return the font style for the item
   */
  public FontStyle getFontStyle() {
    return FontStyle.NORMAL;
  }

  /**
   * Set the rotation for the text.
   *
   * @param rotation the rotation for the text, in degrees
   */
  public void setRotation(double rotation) {}

  /**
   * Get the rotation for the text.
   *
   * @return the rotation for the text, in degrees
   */
  public double getRotation() {
    return -1.0;
  }

  /**
   * Set the X position for the item.
   *
   * @param x the distance, in pixels, from the left side of the board
   */
  public void setX(int x) {}

  /**
   * Get the X position for the item.
   *
   * @return the distance, in pixels, from the left side of the board
   */
  public int getX() {
    return -1;
  }

  /**
   * Set the Y position for the item.
   *
   * @param y the distance, in pixels, from the top of the board
   */
  public void setY(int y) {}

  /**
   * Get the Y position for the item.
   *
   * @return the distance, in pixels, from the top of the board
   */
  public int getY() {
    return -1;
  }

  /**
   * Set the height for the text.
   *
   * @param height the height of the text, in pixels
   */
  public void setHeight(int height) {}

  /**
   * Get the height for the text.
   *
   * @return the height of the text, in pixels
   */
  public int getHeight() {
    return -1;
  }
}
