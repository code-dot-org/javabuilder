package org.code.theater;

import static org.code.theater.Scene.*;
import static org.code.theater.support.DrawImageAction.UNSPECIFIED;
import static org.junit.jupiter.api.Assertions.*;

import org.code.media.Color;
import org.code.media.Font;
import org.code.media.FontStyle;
import org.code.media.Image;
import org.code.theater.support.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SceneTest {

  private Scene unitUnderTest;

  @BeforeEach
  public void setUp() {
    unitUnderTest = new Scene();
  }

  @Test
  public void testClearScene() {
    unitUnderTest.clear(Color.AQUA);
    assertEquals(Color.AQUA, getLastAction(ClearSceneAction.class).getColor());
  }

  @Test
  public void testPlaySound() {
    final double[] samples = {1.0, 0.0, 1.0, 0.0};
    unitUnderTest.playSound(samples);
    assertNotSame(samples, getLastAction(PlaySoundAction.class).getSamples());
    assertEquals(1.0, getLastAction(PlaySoundAction.class).getSamples()[0]);
  }

  @Test
  public void testPlayNote() {
    final Instrument instrument = Instrument.PIANO;
    final int note = 84;
    final double duration = 2.0;
    unitUnderTest.playNote(instrument, note, duration);
    assertEquals(instrument, getLastAction(PlayNoteAction.class).getInstrument());
    assertEquals(note, getLastAction(PlayNoteAction.class).getNote());
    assertEquals(duration, getLastAction(PlayNoteAction.class).getSeconds());
  }

  @Test
  public void testPlayNoteWithDefaultInstrument() {
    final int note = 84;
    final double duration = 2.0;
    unitUnderTest.playNote(note, duration);
    assertEquals(DEFAULT_INSTRUMENT, getLastAction(PlayNoteAction.class).getInstrument());
    assertEquals(note, getLastAction(PlayNoteAction.class).getNote());
    assertEquals(duration, getLastAction(PlayNoteAction.class).getSeconds());
  }

  @Test
  public void testPause() {
    final double duration = 2.0;
    unitUnderTest.pause(duration);
    assertEquals(duration, getLastAction(PauseAction.class).getSeconds());
  }

  @Test
  public void testDrawImage() {
    final Image image = new Image(1, 1);
    final int x = 100;
    final int y = 100;
    final int size = 200;
    final int width = 150;
    final int height = 150;
    final double rotation = 90.0;

    unitUnderTest.drawImage(image, x, y, size);
    // Confirm we copy the image object when drawing it
    assertNotSame(image, getLastAction(DrawImageAction.class).getImage());
    assertEquals(1, getLastAction(DrawImageAction.class).getImage().getHeight());

    assertEquals(x, getLastAction(DrawImageAction.class).getX());
    assertEquals(y, getLastAction(DrawImageAction.class).getY());
    assertEquals(size, getLastAction(DrawImageAction.class).getSize());
    assertEquals(UNSPECIFIED, getLastAction(DrawImageAction.class).getWidth());
    assertEquals(UNSPECIFIED, getLastAction(DrawImageAction.class).getHeight());

    unitUnderTest.drawImage(image, x, y, width, height, rotation);
    assertEquals(width, getLastAction(DrawImageAction.class).getWidth());
    assertEquals(height, getLastAction(DrawImageAction.class).getHeight());
    assertEquals(rotation, getLastAction(DrawImageAction.class).getRotation());
  }

  @Test
  public void testDrawText() {
    final int x = 100;
    final int y = 100;
    final String text = "text123";
    unitUnderTest.drawText(text, x, y);
    assertSame(text, getLastAction(DrawTextAction.class).getText());
    assertEquals(x, getLastAction(DrawTextAction.class).getX());
    assertEquals(y, getLastAction(DrawTextAction.class).getY());
  }

  @Test
  public void testDrawLine() {
    final int startX = 5;
    final int startY = 10;
    final int endX = 15;
    final int endY = 20;
    unitUnderTest.drawLine(startX, startY, endX, endY);
    assertEquals(startX, getLastAction(DrawLineAction.class).getStartX());
    assertEquals(startY, getLastAction(DrawLineAction.class).getStartY());
    assertEquals(endX, getLastAction(DrawLineAction.class).getEndX());
    assertEquals(endY, getLastAction(DrawLineAction.class).getEndY());
  }

  @Test
  public void testDrawPolygon() {
    final int x = 100;
    final int y = 100;
    final int sides = 5;
    final int radius = 100;
    unitUnderTest.drawRegularPolygon(x, y, sides, radius);
    assertEquals(x, getLastAction(DrawPolygonAction.class).getX());
    assertEquals(y, getLastAction(DrawPolygonAction.class).getY());
    assertEquals(sides, getLastAction(DrawPolygonAction.class).getSides());
    assertEquals(radius, getLastAction(DrawPolygonAction.class).getRadius());
  }

  @Test
  public void testDrawShape() {
    final int[] points = {1, 2, 3, 4, 5, 6};
    final boolean close = true;
    unitUnderTest.drawShape(points, close);
    assertEquals(points, getLastAction(DrawShapeAction.class).getPoints());
    assertEquals(close, getLastAction(DrawShapeAction.class).isClosed());
  }

  @Test
  public void testDrawEllipse() {
    final int x = 100;
    final int y = 100;
    final int width = 150;
    final int height = 150;
    unitUnderTest.drawEllipse(x, y, width, height);
    assertEquals(x, getLastAction(DrawEllipseAction.class).getX());
    assertEquals(y, getLastAction(DrawEllipseAction.class).getY());
    assertEquals(width, getLastAction(DrawEllipseAction.class).getWidth());
    assertEquals(height, getLastAction(DrawEllipseAction.class).getHeight());
  }

  @Test
  public void testDrawRectangle() {
    final int x = 100;
    final int y = 100;
    final int width = 150;
    final int height = 150;
    unitUnderTest.drawRectangle(x, y, width, height);
    assertEquals(x, getLastAction(DrawRectangleAction.class).getX());
    assertEquals(y, getLastAction(DrawRectangleAction.class).getY());
    assertEquals(width, getLastAction(DrawRectangleAction.class).getWidth());
    assertEquals(height, getLastAction(DrawRectangleAction.class).getHeight());
  }

  @Test
  public void testSetTextStyle() {
    unitUnderTest.drawText("text", 10, 10);
    assertEquals(DEFAULT_FONT, getLastAction(DrawTextAction.class).getFont());
    assertEquals(DEFAULT_FONT_STYLE, getLastAction(DrawTextAction.class).getFontStyle());

    unitUnderTest.setTextStyle(Font.SANS, FontStyle.ITALIC);
    unitUnderTest.drawText("text", 10, 10);

    assertEquals(Font.SANS, getLastAction(DrawTextAction.class).getFont());
    assertEquals(FontStyle.ITALIC, getLastAction(DrawTextAction.class).getFontStyle());
  }

  @Test
  public void testSetTextHeight() {
    unitUnderTest.drawText("text", 10, 10);
    assertEquals(DEFAULT_TEXT_HEIGHT, getLastAction(DrawTextAction.class).getHeight());

    final int newHeight = 150;
    unitUnderTest.setTextHeight(newHeight);
    unitUnderTest.drawText("text", 10, 10);

    assertEquals(newHeight, getLastAction(DrawTextAction.class).getHeight());
  }

  @Test
  public void setTextColor() {
    unitUnderTest.drawText("text", 10, 10);
    assertSame(DEFAULT_COLOR, getLastAction(DrawTextAction.class).getColor());

    final Color newColor = Color.FUCHSIA;
    unitUnderTest.setTextColor(newColor);
    unitUnderTest.drawText("text", 10, 10);

    assertSame(newColor, getLastAction(DrawTextAction.class).getColor());
  }

  @Test
  public void testSetStrokeColor() {
    unitUnderTest.drawRectangle(1, 1, 100, 100);
    assertSame(DEFAULT_COLOR, getLastAction(DrawRectangleAction.class).getStrokeColor());

    final Color newColor = Color.GOLD;
    unitUnderTest.setStrokeColor(newColor);
    unitUnderTest.drawRectangle(1, 1, 10, 10);

    assertSame(newColor, getLastAction(DrawRectangleAction.class).getStrokeColor());
  }

  @Test
  public void testSetFillColor() {
    unitUnderTest.drawRectangle(1, 1, 100, 100);
    assertSame(DEFAULT_COLOR, getLastAction(DrawRectangleAction.class).getFillColor());

    final Color newColor = Color.INDIGO;
    unitUnderTest.setFillColor(newColor);
    unitUnderTest.drawRectangle(1, 1, 10, 10);

    assertSame(newColor, getLastAction(DrawRectangleAction.class).getFillColor());
  }

  @Test
  public void testSetStrokeWidth() {
    unitUnderTest.drawRectangle(1, 1, 100, 100);
    assertEquals(DEFAULT_STROKE_WIDTH, getLastAction(DrawRectangleAction.class).getStrokeWidth());

    final int newWidth = 15;
    unitUnderTest.setStrokeWidth(newWidth);
    unitUnderTest.drawRectangle(1, 1, 10, 10);

    assertEquals(newWidth, getLastAction(DrawRectangleAction.class).getStrokeWidth());
  }

  private <T extends SceneAction> T getLastAction(Class<T> actionClass) {
    return actionClass.cast(unitUnderTest.getActions().get(unitUnderTest.getActions().size() - 1));
  }
}
