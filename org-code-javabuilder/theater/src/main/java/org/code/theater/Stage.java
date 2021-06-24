package org.code.theater;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import org.code.media.Color;
import org.code.media.Image;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.OutputAdapter;

public class Stage {
  private BufferedImage image;
  private final OutputAdapter outputAdapter;
  private final GifWriter gifWriter;
  private final ByteArrayOutputStream outputStream;
  private java.awt.Color strokeColor;
  private java.awt.Color fillColor;
  private Graphics2D graphics;
  private boolean hasPlayed;

  private static final int WIDTH = 400;
  private static final int HEIGHT = 400;

  /**
   * Initialize Stage with a default image. Stage should be initialized outside of org.code.theater
   * using Theater.stage.
   */
  protected Stage() {
    this(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB));
  }

  /**
   * Initialize Stage with a specific BufferedImage. Used directly for testing. Stage should be
   * initialized outside of org.code.theater using Theater.stage.
   *
   * @param image
   */
  protected Stage(BufferedImage image) {
    this.image = image;
    this.graphics = this.image.createGraphics();
    this.outputAdapter = GlobalProtocol.getInstance().getOutputAdapter();
    this.outputStream = new ByteArrayOutputStream();
    this.gifWriter = new GifWriter(this.outputStream);
    this.hasPlayed = false;

    // set up the image for drawing (set a white background and black stroke/fill)
    this.reset();

    System.setProperty("java.awt.headless", "true");
  }

  /** Returns the width of the theater canvas. */
  public int getWidth() {
    return this.image.getWidth();
  }

  /** Returns the height of the theater canvas. */
  public int getHeight() {
    return this.image.getHeight();
  }

  /**
   * Plays the array of samples provided.
   *
   * @param sound an array of samples to play.
   */
  public void playSound(double[] sound) {}

  /**
   * Plays the sound referenced by the file name.
   *
   * @param filename the file to play in the asset manager.
   * @throws FileNotFoundException if the file can't be found in the project.
   */
  public void playSound(String filename) throws FileNotFoundException {}

  /**
   * Plays a note with the selected instrument.
   *
   * @param instrument the instrument to play.
   * @param note the note to play. 60 represents middle C on a piano.
   * @param seconds length of the note. Implementer's note: Behind the scenes, this should just be
   *     implemented using an array of loopable sounds (Mike can generate these).
   */
  public void playNote(Instrument instrument, int note, double seconds) {}

  /**
   * Wait the provided number of seconds before performing the next draw or play command.
   *
   * @param seconds The number of seconds to wait. This can be a fraction of a second, but the
   *     smallest value can be .1 seconds.
   */
  public void pause(double seconds) {
    this.gifWriter.writeToGif(this.image, (int) (seconds * 1000));
  }

  /** Clear everything, starting from the beginning and emptying the canvas. */
  public void reset() {
    this.graphics.setBackground(java.awt.Color.WHITE);
    // clearRect resets the background with the new color
    this.graphics.clearRect(0, 0, this.getWidth(), this.getHeight());
    this.strokeColor = java.awt.Color.BLACK;
    this.fillColor = java.awt.Color.BLACK;
  }

  /**
   * Draw an image on the canvas at the given location, expanded or shrunk to fit the width and
   * height provided
   *
   * @param filename the name of the file in the asset manager
   * @param x the left side of the image in the canvas
   * @param y the top of the image in the canvas
   * @param width the width to draw the image on the canvas
   * @param height the height to draw the image on the canvas
   * @param rotation the amount to rotate the image
   * @throws FileNotFoundException if the file can't be found in the project.
   */
  public void drawImage(String filename, int x, int y, int width, int height, double rotation)
      throws FileNotFoundException {}

  /**
   * Draw an image on the canvas at the given location, expanded or shrunk to fit the width and
   * height provided
   *
   * @param image the Image object to draw on the canvas
   * @param x the left side of the image in the canvas
   * @param y the top of the image in the canvas
   * @param width the width to draw the image on the canvas
   * @param height the height to draw the image on the canvas
   * @param rotation the amount to rotate the image
   */
  public void drawImage(Image image, int x, int y, int width, int height, double rotation) {}

  /**
   * Draws text on the image.
   *
   * @param text the text to draw
   * @param x the distance from the left side of the image to draw the text.
   * @param y the distance from the top of the image to draw the text.
   * @param color the color to draw the text, using any CSS color string (e.g. #234 or green)
   * @param font the name of the font to draw the text in
   * @param height the height of the text in pixels.
   * @param rotation the rotation or tilt of the text, in degrees
   */
  public void drawText(
      String text, int x, int y, String color, String font, int height, double rotation) {}

  /**
   * Draw a line on the canvas.
   *
   * @param startX the beginning X coordinate of the line.
   * @param startY the beginning Y coordinate of the line.
   * @param endX the end X coordinate of the line.
   * @param endY the end Y coordinate of the line.
   */
  public void drawLine(int startX, int startY, int endX, int endY) {
    if (this.strokeColor == null) {
      this.graphics.setColor(java.awt.Color.BLACK);
    } else {
      this.graphics.setColor(this.strokeColor);
    }
    this.graphics.drawLine(startX, startY, endX, endY);
  }

  /**
   * Draw a regular polygon on the canvas.
   *
   * @param x the center X coordinate of the polygon
   * @param y the center Y coordinate of the polygon
   * @param sides the number of sides of the polygon
   * @param radius the distance from the center to each point on the polygon
   */
  public void drawRegularPolygon(int x, int y, int sides, int radius) {
    if (this.strokeColor != null || this.fillColor != null) {
      Polygon polygon = new Polygon();
      double theta = 2 * Math.PI / sides;
      for (int i = 0; i < sides; i++) {
        int xCoordinate = (int) (Math.cos(theta * i) * radius) + x;
        int yCoordinate = (int) (Math.sin(theta * i) * radius) + y;
        polygon.addPoint(xCoordinate, yCoordinate);
      }
      if (this.strokeColor != null) {
        this.graphics.setColor(this.strokeColor);
        this.graphics.drawPolygon(polygon);
      }
      if (this.fillColor != null) {
        this.graphics.setColor(this.fillColor);
        this.graphics.fillPolygon(polygon);
      }
    }
  }

  /**
   * Draw as a shape by connecting the points provided.
   *
   * @param points an array of numbers representing the points. For instance, a triangle could be
   *     represented as [x1, y1, x2, y2, x3, y3].
   * @param close whether to close the shape. If this is set to true, the last point and the first
   *     point will be connected by a line, and if a fill color is set, the shape will be filled
   *     with that color.
   */
  public void drawShape(int[] points, boolean close) {
    // points should contain an even number of values to represent (x,y) coordinates
    // We need at least 4 points to draw a single line
    if (points.length % 2 != 0 || points.length < 4) {
      throw new TheaterRuntimeException(ExceptionKeys.INVALID_SHAPE);
    }
    if (close) {
      // For a closed shape, use draw/fill shape functions
      if (this.strokeColor != null || this.fillColor != null) {
        int numPoints = points.length / 2;
        int[] xPoints = new int[numPoints];
        int[] yPoints = new int[numPoints];
        // split points into x and y arrays
        for (int i = 0; i < points.length - 1; i += 2) {
          xPoints[i / 2] = points[i];
          yPoints[i / 2] = points[i + 1];
        }
        if (this.strokeColor != null) {
          this.graphics.setColor(this.strokeColor);
          this.graphics.drawPolygon(xPoints, yPoints, numPoints);
        }
        if (this.fillColor != null) {
          this.graphics.setColor(this.fillColor);
          this.graphics.fillPolygon(xPoints, yPoints, numPoints);
        }
      }
    } else if (this.strokeColor != null) {
      // For an open shape, draw as a series of lines
      this.graphics.setColor(this.strokeColor);
      for (int i = 0; i <= points.length - 4; i += 2) {
        this.graphics.drawLine(points[i], points[i + 1], points[i + 2], points[i + 3]);
      }
    }
  }

  /**
   * Draws an ellipse (an oval or a circle) on the canvas.
   *
   * @param x the left side of the ellipse.
   * @param y the top of the ellipse
   * @param width the width of the ellipse
   * @param height the height of the ellipse
   */
  public void drawEllipse(int x, int y, int width, int height) {
    if (this.fillColor != null) {
      this.graphics.setColor(this.fillColor);
      this.graphics.fillOval(x, y, width, height);
    }
    if (this.strokeColor != null) {
      this.graphics.setColor(this.strokeColor);
      this.graphics.drawOval(x, y, width, height);
    }
  }

  /**
   * Draws a rectangle on the canvas.
   *
   * @param x the left side of the rectangle.
   * @param y the top of the rectangle.
   * @param width the width of the rectangle.
   * @param height the height of the rectangle.
   */
  public void drawRectangle(int x, int y, int width, int height) {
    if (this.fillColor != null) {
      this.graphics.setColor(this.fillColor);
      this.graphics.fillRect(x, y, width, height);
    }
    if (this.strokeColor != null) {
      this.graphics.setColor(this.strokeColor);
      this.graphics.drawRect(x, y, width, height);
    }
  }

  /**
   * Sets the thickness of lines drawn.
   *
   * @param width width in pixels of the line to draw. Zero means no line.
   */
  public void setStrokeWidth(double width) {
    this.graphics.setStroke(new BasicStroke((float) width));
  }

  /**
   * Sets the color of lines drawn.
   *
   * @param color the color to draw lines with.
   */
  public void setStrokeColor(Color color) {
    this.strokeColor = this.convertColor(color);
  }

  /** Removes the stroke color so any shapes have no outlines. */
  public void removeStrokeColor() {
    this.strokeColor = null;
  }

  /**
   * Sets the fill color for all shapes drawn
   *
   * @param color the color value to fill any shape.
   */
  public void setFillColor(Color color) {
    this.fillColor = this.convertColor(color);
  }

  /** Removes the fill color so any shapes have no fill. */
  public void removeFillColor() {
    this.fillColor = null;
  }

  /** Plays the instructions. */
  public void play() {
    if (this.hasPlayed) {
      throw new TheaterRuntimeException(ExceptionKeys.DUPLICATE_PLAY_COMMAND);
    } else {
      this.gifWriter.writeToGif(this.image, 0);
      this.gifWriter.close();
      outputAdapter.sendMessage(ImageEncoder.encodeStreamToMessage(this.outputStream));
      this.hasPlayed = true;
    }
  }

  private java.awt.Color convertColor(Color color) {
    return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
  }
}
