package org.code.theater.support;

import org.code.media.Color;

public class DrawPolygonAction extends PaintableAction {

  private final int x;
  private final int y;
  private final int sides;
  private final int radius;

  public DrawPolygonAction(
      int x, int y, int sides, int radius, Color strokeColor, Color fillColor, double strokeWidth) {
    super(strokeColor, fillColor, strokeWidth);
    this.x = x;
    this.y = y;
    this.sides = sides;
    this.radius = radius;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getSides() {
    return sides;
  }

  public int getRadius() {
    return radius;
  }

  @Override
  public SceneActionType getType() {
    return SceneActionType.DRAW_POLYGON;
  }
}
