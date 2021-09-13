 public interface Item {
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
  
  /**
   * Set the width for the item.
   *
   * @param width the width of the item, in pixels
   */
  public void setWidth(int width);

  /**
   * Get the width for the item.
   *
   * @return the width of the item, in pixels
   */
  public int getWidth();
  
  /**
   * Set the height for the item.
   *
   * @param height the height of the item, in pixels
   */
  public void setHeight(int height);
  
  /**
   * Get the height for the item.
   *
   * @return the height of the item, in pixels
   */
  public int getHeight();
