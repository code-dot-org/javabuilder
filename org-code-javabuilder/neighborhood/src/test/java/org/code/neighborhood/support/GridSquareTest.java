package org.code.neighborhood.support;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GridSquareTest {
  private final PrintStream standardOut = System.out;
  private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

  @BeforeEach
  public void setUp() {
    System.setOut(new PrintStream(outputStreamCaptor));
  }

  @AfterEach
  public void tearDown() {
    System.setOut(standardOut);
  }

  @Test
  void wallsNotPassable() {
    GridSquare s = new GridSquare(0, 0);
    assertFalse(s.isPassable());
  }

  @Test
  void obstaclesNotPassable() {
    GridSquare s = new GridSquare(4, 0);
    assertFalse(s.isPassable());
  }

  @Test
  void unknownTileTypeNotPassable() {
    GridSquare s = new GridSquare(-1, 0);
    assertFalse(s.isPassable());
  }

  @Test
  void openTileTypePassable() {
    GridSquare s = new GridSquare(1, 0);
    assertTrue(s.isPassable());
  }

  @Test
  void startTileTypePassable() {
    GridSquare s = new GridSquare(2, 0);
    assertTrue(s.isPassable());
  }

  @Test
  void finishTileTypePassable() {
    GridSquare s = new GridSquare(3, 0);
    assertTrue(s.isPassable());
  }

  @Test
  void startAndFinishTileTypePassable() {
    GridSquare s = new GridSquare(5, 0);
    assertTrue(s.isPassable());
  }

  @Test
  void constructorWithValueProvidedSetsPaintCount() {
    GridSquare s = new GridSquare(1, 0, 4);
    assertTrue(s.containsPaint());
  }

  @Test
  void defaultPaintCountIsNoPaint() {
    GridSquare s = new GridSquare(1, 0);
    assertFalse(s.containsPaint());
  }

  @Test
  void setColorChecksColorFormatBeforeSettingColor() {
    GridSquare s = new GridSquare(1, 0);
    s.setColor("red");
    assertEquals(s.getColor(), "red");
    Exception exception =
        assertThrows(
            NeighborhoodRuntimeException.class,
            () -> {
              s.setColor("r");
            });
    String expectedMessage = ExceptionKeys.INVALID_COLOR.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }

  @Test
  void setColorThrowsExceptionIfThereIsPaint() {
    GridSquare s = new GridSquare(1, 0, 4);
    Exception exception =
        assertThrows(
            NeighborhoodRuntimeException.class,
            () -> {
              s.setColor("red");
            });
    String expectedMessage = ExceptionKeys.INVALID_PAINT_LOCATION.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }

  @Test
  void removePaint() {
    GridSquare s = new GridSquare(1, 0);
    s.setColor("red");
    assertEquals(s.getColor(), "red");
    s.removePaint();
    assertEquals(s.getColor(), null);
  }

  @Test
  void removePaintPrintsErrorWhenNoPaint() {
    GridSquare s = new GridSquare(1, 0);
    assertEquals(s.getColor(), null);
    s.removePaint();
    assertTrue(outputStreamCaptor.toString().trim().contains("There's no paint to remove here"));
  }

  @Test
  void containsPaint() {
    GridSquare noPaint = new GridSquare(1, 0);
    assertFalse(noPaint.containsPaint());

    GridSquare withPaint = new GridSquare(1, 0, 2);
    assertTrue(withPaint.containsPaint());
  }

  @Test
  void collectPaint() {
    GridSquare s = new GridSquare(1, 0, 2);
    assertTrue(s.containsPaint());
    s.collectPaint();
    // paintCount should be 1
    assertTrue(s.containsPaint());
    s.collectPaint();
    // paintCount should be 0
    assertFalse(s.containsPaint());
    s.collectPaint();
    assertTrue(outputStreamCaptor.toString().trim().contains("There's no paint to collect here"));
    // paintCount should be 0
    assertFalse(s.containsPaint());
  }

  @Test
  void hasColor() {
    GridSquare s = new GridSquare(1, 0);
    assertFalse(s.hasColor());
    s.setColor("red");
    assertTrue(s.hasColor());
  }
}
