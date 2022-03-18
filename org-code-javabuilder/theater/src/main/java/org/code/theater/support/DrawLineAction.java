package org.code.theater.support;

import org.code.media.Color;

public class DrawLineAction implements SceneAction {

  private final int startX;
  private final int startY;
  private final int endX;
  private final int endY;
  private final Color color;
  private final double strokeWidth;

  public DrawLineAction(
      int startX, int startY, int endX, int endY, Color color, double strokeWidth) {
    this.startX = startX;
    this.startY = startY;
    this.endX = endX;
    this.endY = endY;
    this.color = color;
    this.strokeWidth = strokeWidth;
  }

  public int getStartX() {
    return startX;
  }

  public int getStartY() {
    return startY;
  }

  public int getEndX() {
    return endX;
  }

  public int getEndY() {
    return endY;
  }

  public Color getColor() {
    return color;
  }

  public double getStrokeWidth() {
    return strokeWidth;
  }

  @Override
  public SceneActionType getType() {
    return SceneActionType.DRAW_LINE;
  }
}
