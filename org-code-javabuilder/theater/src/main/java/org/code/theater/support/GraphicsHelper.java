package org.code.theater.support;

import static org.code.theater.support.Constants.THEATER_HEIGHT;
import static org.code.theater.support.Constants.THEATER_WIDTH;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import org.code.media.Color;
import org.code.media.Font;
import org.code.media.FontHelper;
import org.code.media.FontStyle;

/**
 * Helper class for various {@link Graphics2D} operations (drawing images, lines, shapes, text, etc)
 */
class GraphicsHelper {
  public static class Factory {
    public GraphicsHelper createGraphicsHelper(Graphics2D graphics, FontHelper fontHelper) {
      return new GraphicsHelper(graphics, fontHelper);
    }
  }

  private final Graphics2D graphics;
  private final FontHelper fontHelper;

  public GraphicsHelper(Graphics2D graphics, FontHelper fontHelper) {
    this.graphics = graphics;
    this.fontHelper = fontHelper;
  }

  public void clear(Color color) {
    this.graphics.setBackground(Color.convertToAWTColor(color));
    // clearRect resets the background with the new color
    this.graphics.clearRect(0, 0, THEATER_WIDTH, THEATER_HEIGHT);
  }

  public void drawImage(BufferedImage image, int x, int y, int width, int height, double degrees) {
    if (degrees != 0) {
      AffineTransform transform = new AffineTransform();
      double widthScale = (double) width / image.getWidth();
      double heightScale = (double) height / image.getHeight();
      // create a transform that moves the location of the image to (x,y), rotates around
      // the top left corner of the image and scales the image to width and height
      // Note: order of transforms is important, do not reorder these calls
      transform.translate(x, y);
      transform.rotate(Math.toRadians(degrees), 0, 0);
      transform.scale(widthScale, heightScale);
      this.graphics.drawImage(image, transform, null);
    } else {
      this.graphics.drawImage(image, x, y, width, height, null);
    }
  }

  public void drawText(
      String text,
      int x,
      int y,
      Color color,
      Font font,
      FontStyle fontStyle,
      int height,
      double rotation) {
    AffineTransform originalTransform = this.graphics.getTransform();
    if (rotation != 0) {
      this.graphics.rotate(Math.toRadians(rotation), x, y);
    }
    java.awt.Font sizedFont = this.fontHelper.getFont(font, fontStyle).deriveFont((float) height);
    this.graphics.setFont(sizedFont);
    this.graphics.setColor(Color.convertToAWTColor(color));
    this.graphics.drawString(text, x, y);
    if (rotation != 0) {
      // reset to original transform if we rotated
      this.graphics.setTransform(originalTransform);
    }
  }

  public void drawLine(Color strokeColor, int startX, int startY, int endX, int endY) {
    this.graphics.setColor(Color.convertToAWTColor(strokeColor));
    this.graphics.drawLine(startX, startY, endX, endY);
  }

  public void drawRegularPolygon(
      int x, int y, int sides, int radius, Color strokeColor, Color fillColor) {
    if (strokeColor == null && fillColor == null) {
      return;
    }
    Polygon polygon = new Polygon();
    double theta = 2 * Math.PI / sides;
    for (int i = 0; i < sides; i++) {
      int xCoordinate = (int) (Math.cos(theta * i) * radius) + x;
      int yCoordinate = (int) (Math.sin(theta * i) * radius) + y;
      polygon.addPoint(xCoordinate, yCoordinate);
    }
    if (strokeColor != null) {
      this.graphics.setColor(Color.convertToAWTColor(strokeColor));
      this.graphics.drawPolygon(polygon);
    }
    if (fillColor != null) {
      this.graphics.setColor(Color.convertToAWTColor(fillColor));
      this.graphics.fillPolygon(polygon);
    }
  }

  public void drawShape(int[] points, boolean close, Color strokeColor, Color fillColor) {
    // points should contain an even number of values to represent (x,y) coordinates
    // We need at least 4 points to draw a single line
    if (points.length % 2 != 0 || points.length < 4) {
      throw new TheaterRuntimeException(ExceptionKeys.INVALID_SHAPE);
    }
    if (close) {
      if (strokeColor == null && fillColor == null) {
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
      if (strokeColor != null) {
        this.graphics.setColor(Color.convertToAWTColor(strokeColor));
        this.graphics.drawPolygon(xPoints, yPoints, numPoints);
      }
      if (fillColor != null) {
        this.graphics.setColor(Color.convertToAWTColor(fillColor));
        this.graphics.fillPolygon(xPoints, yPoints, numPoints);
      }
    } else if (strokeColor != null) {
      // For an open shape, draw as a series of lines
      this.graphics.setColor(Color.convertToAWTColor(strokeColor));
      for (int i = 0; i <= points.length - 4; i += 2) {
        this.graphics.drawLine(points[i], points[i + 1], points[i + 2], points[i + 3]);
      }
    }
  }

  public void drawEllipse(int x, int y, int width, int height, Color strokeColor, Color fillColor) {
    if (fillColor != null) {
      this.graphics.setColor(Color.convertToAWTColor(fillColor));
      this.graphics.fillOval(x, y, width, height);
    }
    if (strokeColor != null) {
      this.graphics.setColor(Color.convertToAWTColor(strokeColor));
      this.graphics.drawOval(x, y, width, height);
    }
  }

  public void drawRectangle(
      int x, int y, int width, int height, Color strokeColor, Color fillColor) {
    if (fillColor != null) {
      this.graphics.setColor(Color.convertToAWTColor(fillColor));
      this.graphics.fillRect(x, y, width, height);
    }
    if (strokeColor != null) {
      this.graphics.setColor(Color.convertToAWTColor(strokeColor));
      this.graphics.drawRect(x, y, width, height);
    }
  }

  public void setStrokeWidth(double width) {
    this.graphics.setStroke(new BasicStroke((float) width));
  }
}
