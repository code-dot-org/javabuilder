 public class TextItem implements Item {
    /**
     * Creates text item that can be placed the board.
     *
     * @param text the text to draw
     * @param x the distance from the left side of the board to draw the text.
     * @param y the distance from the top of the board to draw the text.
     * @param color the color to draw the text.
     * @param font the name of the font to draw the text in.
     * @param fontStyle the style of the font.
     * @param height the height of the text in pixels.
     * @param rotation the rotation or tilt of the text, in degrees.
     */
    public TextItem(String text, Color color, Font font, FontStyle fontStyle, int height, double rotation);
   
    /**
     * Creates text item that can be placed the board in a normal font style.
     *
     * @param text the text to draw
     * @param x the distance from the left side of the board to draw the text.
     * @param y the distance from the top of the board to draw the text.
     * @param color the color to draw the text.
     * @param font the name of the font to draw the text in.
     * @param height the height of the text in pixels.
     * @param rotation the rotation or tilt of the text, in degrees.
     */
   public TextItem(String text, Color color, Font font, int height, double rotation);
   
   
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
   * Set the height for the text.
   *
   * @param height the height of the text, in pixels
   */
  public void setHeight(int height);
  
  /**
   * Get the height for the text.
   *
   * @return the height of the text, in pixels
   */
  public int getHeight();
}
