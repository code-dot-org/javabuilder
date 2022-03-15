package org.code.theater.support;

import org.code.media.Color;

public class DrawRectangleAction extends PaintableAction {

  private final int x;
  private final int y;
  private final int width;
  private final int height;

  public DrawRectangleAction(
      int x, int y, int width, int height, Color strokeColor, Color fillColor, double strokeWidth) {
    super(strokeColor, fillColor, strokeWidth);
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  @Override
  public SceneActionType getType() {
    return SceneActionType.DRAW_RECTANGLE;
  }
}
