package org.code.validation;

/** User-facing class representing an (x, y) position and a direction. */
public class Position {
  private final int x;
  private final int y;
  private final String direction;

  public Position(int x, int y, String direction) {
    this.x = x;
    this.y = y;
    this.direction = direction;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  public String getDirection() {
    return this.direction;
  }
}
