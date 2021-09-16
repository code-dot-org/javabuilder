package org.code.playground;

interface Item {
   /**
   * Set the X position for the item.
   *
   * @param x the distance, in pixels, from the left side of the board
   */
  public void setX(int x);

  /**
   * Get the X position for the item.
   *
   * @return the distance, in pixels, from the left side of the board
   */
  public int getX();

  /**
   * Set the Y position for the item.
   *
   * @param y the distance, in pixels, from the top of the board
   */
  public void setY(int y);

  /**
   * Get the Y position for the item.
   *
   * @return the distance, in pixels, from the top of the board
   */
  public int getY();
}
