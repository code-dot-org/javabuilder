package org.code.theater;

import org.code.media.Color;
import org.code.media.Font;
import org.code.media.FontStyle;
import org.code.media.Image;

public class Scene {
  
  public Scene();

  /** Returns the width of the theater canvas. */
  public final int getWidth();

  /** Returns the height of the theater canvas. */
  public final int getHeight();

  /**
   * Clear the canvas and set the background to the given color name.
   *
   * @param color new background color name.
   */
  public final void clear(String color);

  /**
   * Clear the canvas and set the background to the given color.
   *
   * @param color new background color
   */
  public final void clear(Color color);

  /**
   * Plays the array of samples provided.
   *
   * @param sound an array of samples to play.
   */
  public final void playSound(double[] sound);

  /**
   * Plays the sound referenced by the file name.
   *
   * @param filename the file to play in the asset manager.
    */
  public final void playSound(String filename);

  /**
   * Plays a note with the selected instrument.
   *
   * @param instrument the instrument to play.
   * @param note the note to play. 60 represents middle C on a piano.
   * @param seconds length of the note.
   */
  public final void playNote(Instrument instrument, int note, double seconds);

  /**
   * Plays a note with the selected instrument and adds a pause in drawing/audio for the duration of
   * the note, so that subsequent play commands begin after the note has finished playing.
   * Convenience method for playing notes in sequence without needing to call pause() between each
   * one.
   *
   * @param instrument the instrument to play.
   * @param note the note to play. 60 represents middle C on a piano.
   * @param seconds length of the note.
   */
  public final void playNoteAndPause(Instrument instrument, int note, double seconds);

  /**
   * Wait the provided number of seconds before performing the next draw or play command.
   *
   * @param seconds The number of seconds to wait. This can be a fraction of a second, but the
   *     smallest value can be .1 seconds.
   */
  public final void pause(double seconds);

 /**
   * Draw an image on the canvas at the given location, size and rotation
   *
   * @param image the Image object to draw on the canvas
   * @param x the left side of the image in the canvas
   * @param y the top of the image in the canvas
   * @param size the width of the image, in pixels. The height will stretch to make sure the
        image does not appear distorted.
   * @param rotation the amount to rotate the image in degrees
   */
  public final void drawImage(Image image, int x, int y, int size, double rotation);

  /**
   * Draw an image on the canvas at the given location and size
   *
   * @param image the Image object to draw on the canvas
   * @param x the left side of the image in the canvas
   * @param y the top of the image in the canvas
   * @param size the width of the image, in pixels. The height will stretch to make sure the
        image does not appear distorted.
   * @param rotation the amount to rotate the image in degrees
   */
  public final void drawImage(Image image, int x, int y, int size);

  /**
   * Draw an image on the canvas at the given location, expanded or shrunk to fit the width and
   * height provided
   *
   * @param image the Image object to draw on the canvas
   * @param x the left side of the image in the canvas
   * @param y the top of the image in the canvas
   * @param width the width to draw the image on the canvas
   * @param height the height to draw the image on the canvas
   * @param rotation the amount to rotate the image in degrees
   */
  public final void drawImage(Image image, int x, int y, int width, int height, double rotation);

  /**
   * Draw an image on the canvas at the given location, rotation and size
   *
   * @param filename the name of the file in the asset manager
   * @param x the left side of the image in the canvas
   * @param y the top of the image in the canvas
   * @param size the width of the image, in pixels. The height will stretch to make sure the
        image does not appear distorted.
   * @param rotation the amount to rotate the image in degrees
  */
  public final void drawImage(String filename, int x, int y, int size, double rotation);

  /**
   * Draw an image on the canvas at the given location and size
   *
   * @param filename the name of the file in the asset manager
   * @param x the left side of the image in the canvas
   * @param y the top of the image in the canvas
   * @param size the width of the image, in pixels. The height will stretch to make sure the
        image does not appear distorted.
   */
  public final void drawImage(String filename, int x, int y, int size);

  /**
   * Draw an image on the canvas at the given location,expanded or shrunk to fit the width and
   * height provided
   *
   * @param filename the name of the file in the asset manager
   * @param x the left side of the image in the canvas
   * @param y the top of the image in the canvas
   * @param width the width to draw the image on the canvas
   * @param height the height to draw the image on the canvas
   * @param rotation the amount to rotate the image in degrees
   */
  public final void drawImage(String filename, int x, int y, int width, int height, double rotation);

  /**
   * Set the font and style to draw text in, e.g. sans-serif, monospaced, italic, etc.
   *
   * @param font the font to draw with
   * @param style the style of the font
   */
  public final void setTextStyle(Font font, FontStyle style);

  /**
   * Set the size to draw text, defined as the height of the text in pixels
   *
   * @param height the height of the text to draw
   */
  public final void setTextHeight(int height);

  /**
   * Set the color to draw text
   *
   * @param color the name or hex string of the color.
   */
  public final void setTextColor(String color);

  /**
   * Set the color to draw text
   *
   * @param color the color to draw text in
   */
  public final void setTextColor(Color color);

  /**
   * Draws text on the image.
   *
   * @param text the text to draw
   * @param x the distance from the left side of the image to draw the text.
   * @param y the distance from the top of the image to draw the text.
   * @param rotation the rotation or tilt of the text, in degrees
   */
  public final void drawText(String text, int x, int y, double rotation);

  /**
   * Draws text on the image.
   *
   * @param text the text to draw
   * @param x the distance from the left side of the image to draw the text.
   * @param y the distance from the top of the image to draw the text.
   */
  public final void drawText(String text, int x, int y);

  /**
   * Draw a line on the canvas.
   *
   * @param startX the beginning X coordinate of the line.
   * @param startY the beginning Y coordinate of the line.
   * @param endX the end X coordinate of the line.
   * @param endY the end Y coordinate of the line.
   */
  public final void drawLine(int startX, int startY, int endX, int endY);

  /**
   * Draw a regular polygon on the canvas.
   *
   * @param x the center X coordinate of the polygon
   * @param y the center Y coordinate of the polygon
   * @param sides the number of sides of the polygon
   * @param radius the distance from the center to each point on the polygon
   */
  public final void drawRegularPolygon(int x, int y, int sides, int radius);

  /**
   * Draw as a shape by connecting the points provided.
   *
   * @param points an array of numbers representing the points. For instance, a triangle could be
   *     represented as [x1, y1, x2, y2, x3, y3].
   * @param close whether to close the shape. If this is set to true, the last point and the first
   *     point will be connected by a line, and if a fill color is set, the shape will be filled
   *     with that color.
   */
  public final void drawShape(int[] points, boolean close);

  /**
   * Draws an ellipse (an oval or a circle) on the canvas.
   *
   * @param x the left side of the ellipse
   * @param y the top of the ellipse
   * @param width the width of the ellipse
   * @param height the height of the ellipse
   */
  public final void drawEllipse(int x, int y, int width, int height);

  /**
   * Draws a rectangle on the canvas.
   *
   * @param x the left side of the rectangle
   * @param y the top of the rectangle
   * @param width the width of the rectangle
   * @param height the height of the rectangle
   */
  public final void drawRectangle(int x, int y, int width, int height);

  /**
   * Sets the thickness of lines drawn. Note that this only applies to 
   * drawing operations performed by this actor.
   *
   * @param width width in pixels of the line to draw. Zero means no line.
   */
  public final void setStrokeWidth(double width);

  /**
   * Sets the fill color for all shapes drawn. Note that this only applies to 
   * drawing operations performed by this actor.
   *
   * @param color the color value to fill any shape.
   */
  public final void setFillColor(Color color);

  /**
   * Sets the color of lines drawn. Note that this only applies to 
   * drawing operations performed by this actor.
   *
   * @param color the color to draw lines with.
   */
  public final void setStrokeColor(Color color);

  /**
   * Sets the fill color for all shapes drawn. Note that this only applies to 
   * drawing operations performed by this actor.
   *
   * @param color the color name to fill any shape. If the name doesn't match a known color
        or hex value, this call will set the fill color to nothing (e.g. transparent fill).
   */
  public final void setFillColor(String color);

  /**
   * Sets the color of lines drawn.
   *
   * @param color the color name to draw lines with. If the name doesn't match a known color
        or hex value, this call will set the stroke color to nothing (e.g. transparent stroke).
        Note that this only applies to drawing operations performed by this actor.
   */
  public final void setStrokeColor(String color);

  /** Removes the stroke color so any shapes have no stroke. Note that this only applies to 
   * drawing operations performed by this actor.
   */
  public final void removeStrokeColor();

  /** Removes the fill color so any shapes have no fill. Note that this only applies to 
   * drawing operations performed by this actor.
   */
  public final void removeFillColor();
}
