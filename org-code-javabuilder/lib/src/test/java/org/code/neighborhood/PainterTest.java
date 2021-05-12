package org.code.neighborhood;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PainterTest {
  private final PrintStream standardOut = System.out;
  private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
  String singleSquareGrid = "[[\n{\"tileType\": 1}]]";
  String multiSquareGrid =
      "[[\n{\"tileType\": 1}, {\"tileType\": 1}], \n[{\"tileType\": 1}, {\"tileType\": 1}]]";

  @BeforeEach
  public void setUp() {
    System.setOut(new PrintStream(outputStreamCaptor));
    World w = new World(singleSquareGrid);
    World.setInstance(w);
  }

  @AfterEach
  public void tearDown() {
    System.setOut(standardOut);
  }

  @Test
  void constructorThrowsErrorIfDirectionInvalid() {
    Exception exception =
        assertThrows(
            UnsupportedOperationException.class,
            () -> {
              new Painter(0, 0, "not a direction", 5);
            });
    String expectedMessage = ExceptionKeys.INVALID_DIRECTION.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }

  @Test
  void constructorThrowsErrorIfStartLocationInvalid() {
    Exception exception =
        assertThrows(
            UnsupportedOperationException.class,
            () -> {
              new Painter(-1, -1, "West", 5);
            });
    String expectedMessage = ExceptionKeys.INVALID_LOCATION.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }

  @Test
  void canMoveReturnsFalseIfInvalid() {
    Painter painter = new Painter(0, 0, "North", 5);
    assertFalse(painter.canMove("North"));
    assertFalse(painter.canMove("East"));
    assertFalse(painter.canMove("South"));
    assertFalse(painter.canMove("West"));
  }

  @Test
  void moveThrowsErrorIfInvalidMovement() {
    Painter painter = new Painter(0, 0, "North", 5);
    Exception exception =
        assertThrows(
            UnsupportedOperationException.class,
            () -> {
              painter.move();
            });
    String expectedMessage = ExceptionKeys.INVALID_MOVE.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }

  @Test
  void movePrintsNewLocationIfValidMovement() {
    World w = new World(multiSquareGrid);
    World.setInstance(w);
    Painter painter = new Painter(0, 0, "East", 5);
    painter.turnLeft();
    assertTrue(painter.canMove("North"));
    painter.move();
    assertTrue(outputStreamCaptor.toString().trim().contains("New (x,y) : (0,1)"));
  }

  @Test
  void takePaintPrintsErrorIfSquareHasNoPaint() {
    Painter painter = new Painter(0, 0, "North", 5);
    painter.takePaint();
    assertTrue(outputStreamCaptor.toString().trim().contains("There is no paint to collect here"));
  }
}
