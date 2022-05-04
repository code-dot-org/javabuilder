package org.code.theater.support;

import static org.code.theater.support.Constants.THEATER_HEIGHT;
import static org.code.theater.support.Constants.THEATER_WIDTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import org.code.media.Color;
import org.code.media.Font;
import org.code.media.FontStyle;
import org.code.media.support.FontHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphicsHelperTest {

  private Graphics2D graphics;
  private FontHelper fontHelper;
  private GraphicsHelper unitUnderTest;

  @BeforeEach
  public void setUp() {
    graphics = mock(Graphics2D.class);
    fontHelper = mock(FontHelper.class);
    unitUnderTest = new GraphicsHelper(graphics, fontHelper);
  }

  @Test
  void clearSetsBackgroundAndClears() {
    unitUnderTest.clear(Color.BLACK);
    verify(graphics).setBackground(Color.convertToAWTColor(Color.BLACK));
    verify(graphics).clearRect(0, 0, THEATER_WIDTH, THEATER_HEIGHT);
  }

  @Test
  void drawLineDrawsLine() {
    final int startX = 10;
    final int startY = 20;
    final int endX = 100;
    final int endY = 200;
    unitUnderTest.drawLine(Color.BLUE, startX, startY, endX, endY);
    verify(graphics).setColor(Color.convertToAWTColor(Color.BLUE));
    verify(graphics).drawLine(startX, startY, endX, endY);
  }

  @Test
  void removingStrokeAndFillPreventsShapeDrawing() {
    unitUnderTest.drawRegularPolygon(0, 0, 5, 100, null, null);
    verify(graphics, never()).drawPolygon(any());
    verify(graphics, never()).fillPolygon(any());
  }

  @Test
  void removingStrokeStillCallsFill() {
    unitUnderTest.drawEllipse(5, 5, 100, 100, null, Color.BLACK);
    verify(graphics, never()).drawOval(5, 5, 100, 100);
    verify(graphics).fillOval(5, 5, 100, 100);
  }

  @Test
  void removingFillStillCallsStroke() {
    unitUnderTest.drawEllipse(5, 5, 100, 100, Color.BLACK, null);
    verify(graphics, never()).fillOval(5, 5, 100, 100);
    verify(graphics).drawOval(5, 5, 100, 100);
  }

  @Test
  void drawOpenShapeDrawsLines() {
    unitUnderTest.drawShape(new int[] {0, 0, 25, 30, 0, 100}, false, Color.BLACK, Color.INDIGO);
    verify(graphics, times(2)).drawLine(anyInt(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void drawClosedShapeDrawsAndFillsShape() {
    unitUnderTest.drawShape(new int[] {0, 0, 25, 30, 0, 100}, true, Color.BLACK, Color.RED);
    int[] xPoints = new int[] {0, 25, 0};
    int[] yPoints = new int[] {0, 30, 100};
    verify(graphics).drawPolygon(xPoints, yPoints, 3);
    verify(graphics).fillPolygon(xPoints, yPoints, 3);
  }

  @Test
  void throwsExceptionOnInvalidShape() {
    int[] tooFewPoints = new int[] {1, 0};
    int[] oddNumberOfPoints = new int[] {0, 0, 1, 1, 2};
    verifyInvalidShapeThrowsException(unitUnderTest, tooFewPoints, false);
    verifyInvalidShapeThrowsException(unitUnderTest, oddNumberOfPoints, true);
  }

  @Test
  void drawImageWithRotationCreatesTransform() {
    unitUnderTest.drawImage(mock(BufferedImage.class), 0, 300, 50, 75, 90);
    verify(graphics).drawImage(any(BufferedImage.class), any(AffineTransform.class), any());
  }

  @Test
  void drawImageWithoutRotationDoesNotCreateTransform() {
    unitUnderTest.drawImage(mock(BufferedImage.class), 0, 300, 50, 75, 0);
    verify(graphics)
        .drawImage(any(BufferedImage.class), anyInt(), anyInt(), anyInt(), anyInt(), any());
  }

  @Test
  void drawTextWithoutRotationDrawsTextCorrectly() {
    final java.awt.Font awtFont = mock(java.awt.Font.class);
    when(fontHelper.getFont(any(), any())).thenReturn(awtFont);
    when(awtFont.deriveFont(anyFloat())).thenReturn(awtFont);
    unitUnderTest.drawText("hello", 0, 0, Color.BLUE, Font.SANS, FontStyle.BOLD, 12, 0);
    verify(graphics).drawString("hello", 0, 0);
    verify(graphics).setFont(awtFont);
    // verify we never rotate
    verify(graphics, never()).rotate(anyDouble(), anyDouble(), anyDouble());
  }

  @Test
  void drawTextWithRotationDrawsTextCorrectly() {
    final java.awt.Font awtFont = mock(java.awt.Font.class);
    when(fontHelper.getFont(any(), any())).thenReturn(awtFont);
    when(awtFont.deriveFont(anyFloat())).thenReturn(awtFont);
    unitUnderTest.drawText("hello world", 50, 150, Color.BLUE, Font.SANS, FontStyle.BOLD, 12, 95);
    verify(graphics).drawString("hello world", 50, 150);
    double radians = Math.toRadians(95);
    // verify we rotated correctly
    verify(graphics).rotate(radians, 50, 150);
    // verify we reset the transform
    verify(graphics).setTransform(any());
  }

  private void verifyInvalidShapeThrowsException(
      GraphicsHelper graphicsHelper, int[] points, boolean close) {
    Exception exception =
        assertThrows(
            TheaterRuntimeException.class,
            () -> {
              graphicsHelper.drawShape(points, close, Color.BLACK, Color.BLACK);
            });
    String expectedMessage = ExceptionKeys.INVALID_SHAPE.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }
}
