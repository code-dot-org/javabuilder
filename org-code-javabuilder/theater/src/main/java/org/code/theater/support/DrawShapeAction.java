package org.code.theater.support;

import org.code.media.Color;

public class DrawShapeAction extends PaintableAction {

  private final int[] points;
  private final boolean close;

  public DrawShapeAction(
      int[] points, boolean close, Color strokeColor, Color fillColor, double strokeWidth) {
    super(strokeColor, fillColor, strokeWidth);
    this.points = points;
    this.close = close;
  }

  public int[] getPoints() {
    return points;
  }

  public boolean isClosed() {
    return close;
  }

  @Override
  public SceneActionType getType() {
    return SceneActionType.DRAW_SHAPE;
  }
}
