package org.code.theater.support;

import org.code.media.Image;

public class DrawImageAction implements SceneAction {
  public static final int UNSPECIFIED = -1;

  private final Image image;
  private final int x;
  private final int y;
  private final int size;
  private final int width;
  private final int height;
  private final double rotation;

  public DrawImageAction(
      Image image, int x, int y, int size, int width, int height, double rotation) {
    this.image = image;
    this.x = x;
    this.y = y;
    this.size = size;
    this.width = width;
    this.height = height;
    this.rotation = rotation;
  }

  public Image getImage() {
    return image;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getSize() {
    return size;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public double getRotation() {
    return rotation;
  }

  @Override
  public SceneActionType getType() {
    return SceneActionType.DRAW_IMAGE;
  }
}
