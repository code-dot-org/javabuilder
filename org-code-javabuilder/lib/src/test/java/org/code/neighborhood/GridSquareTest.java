package org.code.neighborhood;

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
    GridSquare s = new GridSquare(0);
    assertFalse(s.isPassable());
  }

  @Test
  void obstaclesNotPassable() {
    GridSquare s = new GridSquare(4);
    assertFalse(s.isPassable());
  }

  @Test
  void unknownTileTypeNotPassable() {
    GridSquare s = new GridSquare(-1);
    assertFalse(s.isPassable());
  }

  @Test
  void openTileTypePassable() {
    GridSquare s = new GridSquare(1);
    assertTrue(s.isPassable());
  }

  @Test
  void startTileTypePassable() {
    GridSquare s = new GridSquare(2);
    assertTrue(s.isPassable());
  }

  @Test
  void finishTileTypePassable() {
    GridSquare s = new GridSquare(3);
    assertTrue(s.isPassable());
  }

  @Test
  void startAndFinishTileTypePassable() {
    GridSquare s = new GridSquare(5);
    assertTrue(s.isPassable());
  }

  @Test
  void constructorWithValueProvidedSetsPaintCount() {
    GridSquare s = new GridSquare(1, 4);
    assertTrue(s.containsPaint());
  }

  @Test
  void defaultPaintCountIsNoPaint() {
    GridSquare s = new GridSquare(1);
    assertFalse(s.containsPaint());
  }

  @Test
  void setColorChecksColorFormatBeforeSettingColor() {
    GridSquare s = new GridSquare(1);
    s.setColor("r");
    assertEquals(s.getColor(), "");
    assertTrue(
        outputStreamCaptor
            .toString()
            .trim()
            .contains("Invalid color, please check your color format"));
    s.setColor("red");
    assertEquals(s.getColor(), "red");
    s.setColor("green");
    assertEquals(s.getColor(), "green");
  }

  @Test
  void setColorDoesNotChangeColorIfThereIsPaint() {
    GridSquare s = new GridSquare(1, 4);
    s.setColor("red");
    assertEquals(s.getColor(), "");
  }

  @Test
  void removePaint() {
    GridSquare s = new GridSquare(1);
    s.setColor("red");
    assertEquals(s.getColor(), "red");
    s.removePaint();
    assertEquals(s.getColor(), "");
  }

  @Test
  void removePaintPrintsErrorWhenNoPaint() {
    GridSquare s = new GridSquare(1);
    assertEquals(s.getColor(), "");
    s.removePaint();
    assertTrue(outputStreamCaptor.toString().trim().contains("There's no paint to remove here"));
  }

  @Test
  void containsPaint() {
    GridSquare noPaint = new GridSquare(1);
    assertFalse(noPaint.containsPaint());

    GridSquare withPaint = new GridSquare(1, 2);
    assertTrue(withPaint.containsPaint());
  }

  @Test
  void collectPaint() {
    GridSquare s = new GridSquare(1, 2);
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
  void getPrintableDescriptionReturnsXForNotPassable() {
    GridSquare s = new GridSquare(0);
    assertFalse(s.isPassable());
    assertEquals(s.getPrintableDescription(), "x");
  }

  @Test
  void getPrintableDescriptionReturnsColorForPassableWithColor() {
    GridSquare s = new GridSquare(1);
    assertTrue(s.isPassable());
    s.setColor("red");
    assertEquals(s.getPrintableDescription(), "red");
  }

  @Test
  void getPrintableDescriptionReturnsPaintCountForPassableWithoutColor() {
    GridSquare s = new GridSquare(1, 4);
    assertTrue(s.isPassable());
    assertEquals(s.getPrintableDescription(), "4");
  }

  @Test
  void hasColor() {
    GridSquare s = new GridSquare(1);
    assertFalse(s.hasColor());
    s.setColor("red");
    assertTrue(s.hasColor());
  }
}
