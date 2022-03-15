package org.code.theater.support;

import org.code.media.Color;

public abstract class PaintableAction implements SceneAction {
  private final Color strokeColor;
  private final Color fillColor;
  private final double strokeWidth;

  public PaintableAction(Color strokeColor, Color fillColor, double strokeWidth) {
    this.strokeColor = strokeColor;
    this.fillColor = fillColor;
    this.strokeWidth = strokeWidth;
  }

  public Color getStrokeColor() {
    return strokeColor;
  }

  public Color getFillColor() {
    return fillColor;
  }

  public double getStrokeWidth() {
    return this.strokeWidth;
  }
}
