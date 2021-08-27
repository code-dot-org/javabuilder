package org.code.theater;

import static org.code.protocol.AllowedFileNames.THEATER_AUDIO_NAME;
import static org.code.protocol.AllowedFileNames.THEATER_IMAGE_NAME;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import org.code.media.AudioWriter;
import org.code.media.Color;
import org.code.media.Image;
import org.code.protocol.*;

public class Stage {
  private final BufferedImage image;
  private final OutputAdapter outputAdapter;
  private final JavabuilderFileWriter fileWriter;
  private final GifWriter gifWriter;
  private final ByteArrayOutputStream imageOutputStream;
  private final Graphics2D graphics;
  private final ByteArrayOutputStream audioOutputStream;
  private final AudioWriter audioWriter;
  private final InstrumentSampleLoader instrumentSampleLoader;
  private java.awt.Color strokeColor;
  private java.awt.Color fillColor;
  private boolean hasPlayed;

  private static final int WIDTH = 400;
  private static final int HEIGHT = 400;
  private static final java.awt.Color DEFAULT_COLOR = java.awt.Color.BLACK;

  /**
   * Initialize Stage with a default image. Stage should be initialized outside of org.code.theater
   * using Theater.stage.
   */
  protected Stage() {
    this(
        new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB),
        new GifWriter.Factory(),
        new AudioWriter.Factory(),
        new InstrumentSampleLoader());
  }

  /**
   * Initialize Stage with a specific BufferedImage. Used directly for testing. Stage should be
   * initialized outside of org.code.theater using Theater.stage.
   *
   * @param image
   */
  protected Stage(
      BufferedImage image,
      GifWriter.Factory gifWriterFactory,
      AudioWriter.Factory audioWriterFactory,
      InstrumentSampleLoader instrumentSampleLoader) {
    this.image = image;
    this.graphics = this.image.createGraphics();
    this.outputAdapter = GlobalProtocol.getInstance().getOutputAdapter();
    this.fileWriter = GlobalProtocol.getInstance().getFileWriter();
    this.imageOutputStream = new ByteArrayOutputStream();
    this.gifWriter = gifWriterFactory.createGifWriter(this.imageOutputStream);
    this.audioOutputStream = new ByteArrayOutputStream();
    this.audioWriter = audioWriterFactory.createAudioWriter(this.audioOutputStream);
    this.instrumentSampleLoader = instrumentSampleLoader;
    this.hasPlayed = false;

    // set up the image for drawing (set a white background and black stroke/fill)
    this.clear(Color.WHITE);

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
  public void playSound(double[] sound) {
    this.audioWriter.writeAudioSamples(sound);
  }

  /**
   * Plays the sound referenced by the file name.
   *
   * @param filename the file to play in the asset manager.
   * @throws FileNotFoundException if the file can't be found in the project.
   */
  public void playSound(String filename) throws FileNotFoundException {
    this.audioWriter.writeAudioFromAssetFile(filename);
  }

  /**
   * Plays a note with the selected instrument.
   *
   * @param instrument the instrument to play.
   * @param note the note to play. 60 represents middle C on a piano.
   * @param seconds length of the note.
   */
  public void playNote(Instrument instrument, int note, double seconds) {
    this.playNote(instrument, note, seconds, false);
  }

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
  public void playNoteAndPause(Instrument instrument, int note, double seconds) {
    this.playNote(instrument, note, seconds, true);
  }

  /**
   * Wait the provided number of seconds before performing the next draw or play command.
   *
   * @param seconds The number of seconds to wait. This can be a fraction of a second, but the
   *     smallest value can be .1 seconds.
   */
  public void pause(double seconds) {
    this.gifWriter.writeToGif(this.image, (int) (Math.max(seconds, 0.1) * 1000));
    this.audioWriter.addDelay(Math.max(seconds, 0.1));
  }

  /**
   * Clear the canvas and set the background to the given color
   *
   * @param color new background color
   */
  public void clear(Color color) {
    this.graphics.setBackground(Color.convertToAWTColor(color));
    // clearRect resets the background with the new color
    this.graphics.clearRect(0, 0, this.getWidth(), this.getHeight());
    this.strokeColor = DEFAULT_COLOR;
    this.fillColor = DEFAULT_COLOR;
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
   * @param rotation the amount to rotate the image in degrees
   * @throws FileNotFoundException if the file can't be found in the project.
   */
  public void drawImage(String filename, int x, int y, int width, int height, double rotation)
      throws FileNotFoundException {
    BufferedImage imageToDraw = Image.getImageAssetFromFile(filename);
    this.drawImageHelper(imageToDraw, x, y, width, height, rotation);
  }

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
  public void drawImage(Image image, int x, int y, int width, int height, double rotation) {
    this.drawImageHelper(image.getBufferedImage(), x, y, width, height, rotation);
  }

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
      // Lines will always be drawn even if strokeColor is null. Use default color in this case.
      this.graphics.setColor(DEFAULT_COLOR);
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
    if (this.strokeColor == null && this.fillColor == null) {
      return;
    }
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
      if (this.strokeColor == null && this.fillColor == null) {
        return;
      }
      // For a closed shape, use draw/fill shape functions
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
    this.strokeColor = Color.convertToAWTColor(color);
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
    this.fillColor = Color.convertToAWTColor(color);
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
      this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.GENERATING_RESULTS));
      this.gifWriter.writeToGif(this.image, 0);
      this.gifWriter.close();
      this.audioWriter.writeToAudioStreamAndClose();
      this.writeImageAndAudioToFile();
      this.hasPlayed = true;
    }
  }

  private void drawImageHelper(
      BufferedImage image, int x, int y, int width, int height, double degrees) {
    if (degrees != 0) {
      AffineTransform transform = new AffineTransform();
      double widthScale = (double) width / image.getWidth();
      double heightScale = (double) height / image.getHeight();
      // create a transform that moves the location of the image to (x,y), rotates around
      // the center of the image and scales the image to width and height
      // Note: order of transforms is important, do not reorder these calls
      transform.translate(x, y);
      transform.rotate(Math.toRadians(degrees), width / 2, height / 2);
      transform.scale(widthScale, heightScale);
      this.graphics.drawImage(image, transform, null);
    } else {
      this.graphics.drawImage(image, x, y, width, height, null);
    }
  }

  private void writeImageAndAudioToFile() {
    try {
      String imageUrl =
          this.fileWriter.writeToFile(
              THEATER_IMAGE_NAME, this.imageOutputStream.toByteArray(), "image/gif");
      String audioUrl =
          this.fileWriter.writeToFile(
              THEATER_AUDIO_NAME, this.audioOutputStream.toByteArray(), "audio/wav");

      HashMap<String, String> imageMessage = new HashMap<>();
      imageMessage.put("url", imageUrl);
      this.outputAdapter.sendMessage(new TheaterMessage(TheaterSignalKey.VISUAL_URL, imageMessage));

      HashMap<String, String> audioMessage = new HashMap<>();
      audioMessage.put("url", audioUrl);
      this.outputAdapter.sendMessage(new TheaterMessage(TheaterSignalKey.AUDIO_URL, audioMessage));
    } catch (JavabuilderException e) {
      // we should not hit this (caused by too many file writes)
      // in normal execution as it is only called via play,
      // and play can only be called once.
      throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
  }

  private void playNote(Instrument instrument, int note, double noteLength, boolean shouldPause) {
    final String sampleFilePath = instrumentSampleLoader.getSampleFilePath(instrument, note);
    if (sampleFilePath == null) {
      return;
    }

    try {
      this.audioWriter.writeAudioFromLocalFile(sampleFilePath, noteLength);
      if (shouldPause) {
        this.pause(noteLength);
      }
    } catch (FileNotFoundException e) {
      System.out.printf("Could not play instrument: %s at note: %s%n", instrument, note);
    }
  }
}
