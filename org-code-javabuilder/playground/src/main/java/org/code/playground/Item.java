package org.code.playground;

public abstract class Item {
  private int xLocation;
  private int yLocation;
  private int height;

  Item(int x, int y, int height) {
    this.xLocation = x;
    this.yLocation = y;
    this.height = height;
  }

  /**
   * Set the X position for the item.
   *
   * @param x the distance, in pixels, from the left side of the board
   */
  public void setX(int x) {
    this.xLocation = x;
  }

  /**
   * Get the X position for the item.
   *
   * @return the distance, in pixels, from the left side of the board
   */
  public int getX() {
    return this.xLocation;
  }

  /**
   * Set the Y position for the item.
   *
   * @param y the distance, in pixels, from the top of the board
   */
  public void setY(int y) {
    this.yLocation = y;
  }

  /**
   * Get the Y position for the item.
   *
   * @return the distance, in pixels, from the top of the board
   */
  public int getY() {
    return this.yLocation;
  }

  /**
   * Set the height for the item.
   *
   * @param height the height of the item, in pixels
   */
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Get the height for the item.
   *
   * @return the height of the item, in pixels
   */
  public int getHeight() {
    return this.height;
  }
}
