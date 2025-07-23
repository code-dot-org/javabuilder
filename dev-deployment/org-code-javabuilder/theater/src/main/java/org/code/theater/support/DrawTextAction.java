package org.code.theater.support;

import org.code.media.Color;
import org.code.media.Font;
import org.code.media.FontStyle;

public class DrawTextAction implements SceneAction {

  private final String text;
  private final int x;
  private final int y;
  private final double rotation;
  private final int height;
  private final Font font;
  private final FontStyle fontStyle;
  private final Color color;

  public DrawTextAction(
      String text,
      int x,
      int y,
      double rotation,
      int height,
      Font font,
      FontStyle fontStyle,
      Color color) {
    this.text = text;
    this.x = x;
    this.y = y;
    this.rotation = rotation;
    this.height = height;
    this.font = font;
    this.fontStyle = fontStyle;
    this.color = color;
  }

  public String getText() {
    return text;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public double getRotation() {
    return rotation;
  }

  public int getHeight() {
    return height;
  }

  public Font getFont() {
    return font;
  }

  public FontStyle getFontStyle() {
    return fontStyle;
  }

  public Color getColor() {
    return color;
  }

  @Override
  public SceneActionType getType() {
    return SceneActionType.DRAW_TEXT;
  }
}
