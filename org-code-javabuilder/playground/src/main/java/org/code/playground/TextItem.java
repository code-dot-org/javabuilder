package org.code.playground;

import java.util.HashMap;
import org.code.media.Color;
import org.code.media.Font;
import org.code.media.FontStyle;
import org.code.protocol.ClientMessageDetailKeys;

public class TextItem extends Item {
  private String text;
  private Font font;
  private FontStyle fontStyle;
  private double rotation;
  private Color color;

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
    // Copy to new color to store the values only, and not a reference to the original color object
    this.color = new Color(color);
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
    this(text, x, y, color, font, FontStyle.NORMAL, height, rotation);
  }

  /**
   * Set the text content for the item.
   *
   * @param text the content for the item
   */
  public void setText(String text) {
    this.text = text;
    this.sendChangeMessage(ClientMessageDetailKeys.TEXT, text);
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
    // Copy to new color to store the values only, and not a reference to the original color object
    this.color = new Color(color);
    HashMap<String, String> colorDetails = new HashMap<>();
    this.addColorToDetails(colorDetails);
    this.sendChangeMessage(colorDetails);
  }

  /**
   * Set the amount of red (ranging from 0 to 255). Values below 0 will be ignored and set to 0, and
   * values above 255 will be ignored and set to 255.
   *
   * @param colorRed the amount of red (ranging from 0 to 255) in the color of the text.
   */
  public void setRed(int colorRed) {
    this.color = Color.copyWithRed(this.color, colorRed);
    this.sendChangeMessage(
        ClientMessageDetailKeys.COLOR_RED, Integer.toString(this.color.getRed()));
  }

  /**
   * Set the amount of green (ranging from 0 to 255). Values below 0 will be ignored and set to 0,
   * and values above 255 will be ignored and set to 255.
   *
   * @param colorGreen the amount of green (ranging from 0 to 255) in the color of the text.
   */
  public void setGreen(int colorGreen) {
    this.color = Color.copyWithGreen(this.color, colorGreen);
    this.sendChangeMessage(
        ClientMessageDetailKeys.COLOR_GREEN, Integer.toString(this.color.getGreen()));
  }

  /**
   * Set the amount of blue (ranging from 0 to 255). Values below 0 will be ignored and set to 0,
   * and values above 255 will be ignored and set to 255.
   *
   * @param colorBlue the amount of blue (ranging from 0 to 255) in the color of the text.
   */
  public void setBlue(int colorBlue) {
    this.color = Color.copyWithBlue(this.color, colorBlue);
    this.sendChangeMessage(
        ClientMessageDetailKeys.COLOR_BLUE, Integer.toString(this.color.getBlue()));
  }

  /**
   * Get the text color for the item.
   *
   * @return the text color for the item
   */
  public Color getColor() {
    // TODO: wrapping this.color in a new Color object will not be necessary once Color is
    // immutable.
    return new Color(this.color);
  }

  /**
   * Set the font for the text.
   *
   * @param font the font for the text.
   */
  public void setFont(Font font) {
    this.font = font;
    this.sendChangeMessage(ClientMessageDetailKeys.FONT, font.toString());
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
    this.sendChangeMessage(ClientMessageDetailKeys.FONT_STYLE, fontStyle.toString());
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
    this.sendChangeMessage(ClientMessageDetailKeys.ROTATION, Double.toString(rotation));
  }

  /**
   * Get the rotation for the text.
   *
   * @return the rotation for the text, in degrees
   */
  public double getRotation() {
    return this.rotation;
  }

  @Override
  protected HashMap<String, String> getDetails() {
    HashMap<String, String> details = super.getDetails();
    details.put(ClientMessageDetailKeys.TEXT, this.getText());
    this.addColorToDetails(details);
    details.put(ClientMessageDetailKeys.FONT, this.getFont().toString());
    details.put(ClientMessageDetailKeys.FONT_STYLE, this.getFontStyle().toString());
    details.put(ClientMessageDetailKeys.ROTATION, Double.toString(this.getRotation()));
    return details;
  }

  private void addColorToDetails(HashMap<String, String> details) {
    details.put(ClientMessageDetailKeys.COLOR_RED, Integer.toString(this.color.getRed()));
    details.put(ClientMessageDetailKeys.COLOR_GREEN, Integer.toString(this.color.getGreen()));
    details.put(ClientMessageDetailKeys.COLOR_BLUE, Integer.toString(this.color.getBlue()));
  }
}
