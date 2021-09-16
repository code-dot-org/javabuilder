package org.code.neighborhood;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.code.protocol.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class PainterTest {
  private final OutputAdapter outputAdapter = mock(OutputAdapter.class);
  private final PrintStream standardOut = System.out;
  private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
  String singleSquareGrid = "[[\n{\"tileType\": 1, \"assetId\": 0}]]";
  String multiSquareGrid =
      "[[\n{\"tileType\": 1, \"value\": 1, \"assetId\": 0}, {\"tileType\": 1, \"value\": 1, \"assetId\": 0}], \n[{\"tileType\": 1, \"value\": 1, \"assetId\": 0}, {\"tileType\": 1, \"value\": 1, \"assetId\": 0}]]";

  @BeforeEach
  public void setUp() {
    GlobalProtocol.create(
        outputAdapter, mock(InputAdapter.class), "", "", "", mock(JavabuilderFileWriter.class));
    System.setOut(new PrintStream(outputStreamCaptor));
    World w = new World(singleSquareGrid);
    World.setInstance(w);
  }

  @AfterEach
  public void tearDown() {
    System.setOut(standardOut);
  }

  @Test
  void defaultConstructorMakesPainterWithNoPaintFacingEast() {
    Painter p = new Painter();
    assertEquals(p.getMyPaint(), 0);
    assertTrue(p.facingEast());
  }

  @Test
  void constructorThrowsErrorIfDirectionInvalid() {
    Exception exception =
        assertThrows(
            NeighborhoodRuntimeException.class,
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
            NeighborhoodRuntimeException.class,
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
  void canMoveReturnsFalseIfCurrentDirectionInvalid() {
    Painter painter = new Painter(0, 0, "North", 5);
    assertFalse(painter.canMove());
  }

  @Test
  void canMoveReturnsTrueIfCurrentDirectionValid() {
    World.setInstance(new World(multiSquareGrid));
    Painter painter = new Painter(0, 0, "South", 5);
    assertTrue(painter.canMove());
  }

  @Test
  void moveThrowsErrorIfInvalidMovement() {
    Painter painter = new Painter(0, 0, "North", 5);
    Exception exception =
        assertThrows(
            NeighborhoodRuntimeException.class,
            () -> {
              painter.move();
            });
    String expectedMessage = ExceptionKeys.INVALID_MOVE.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }

  @Test
  void moveSignalsNewLocationIfValidMovement() {
    World w = new World(multiSquareGrid);
    World.setInstance(w);
    Painter painter = new Painter(0, 0, "West", 5);
    painter.turnLeft();
    ArgumentCaptor<NeighborhoodSignalMessage> message =
        ArgumentCaptor.forClass(NeighborhoodSignalMessage.class);
    verify(outputAdapter, times(2)).sendMessage(message.capture());
    assertEquals(message.getValue().getValue(), "TURN_LEFT");
    assertTrue(message.getValue().getDetail().toString().contains("\"direction\":\"south\""));
    assertTrue(painter.canMove("South"));
    painter.move();
  }

  @Test
  void takePaintPrintsErrorIfSquareHasNoPaint() {
    Painter painter = new Painter(0, 0, "North", 5);
    painter.takePaint();
    assertTrue(outputStreamCaptor.toString().trim().contains("There is no paint to collect here"));
  }

  @Test
  void takePaintIncrementsPaint() {
    World w = new World(multiSquareGrid);
    World.setInstance(w);
    Painter painter = new Painter(0, 0, "North", 5);
    assertEquals(painter.getMyPaint(), 5);
    painter.takePaint();
    assertEquals(painter.getMyPaint(), 6);
  }

  @Test
  void paintPrintsErrorIfNoPaint() {
    Painter painter = new Painter(0, 0, "North", 0);
    painter.paint("red");
    assertTrue(
        outputStreamCaptor
            .toString()
            .trim()
            .contains("There is no more paint in the painter's bucket"));
  }

  @Test
  void paintDecrementsPaint() {
    Painter painter = new Painter(0, 0, "North", 1);
    assertEquals(painter.getMyPaint(), 1);
    painter.paint("red");
    assertEquals(painter.getMyPaint(), 0);
  }

  @Test
  void largeGridInfinitePaintDefaultConstructor() {
    World w = new World(20);
    World.setInstance(w);
    Painter painter = new Painter();
    assertTrue(painter.hasPaint());
  }

  @Test
  void noInfinitePaintDefaultConstructor() {
    World w = new World(19);
    World.setInstance(w);
    Painter painter = new Painter();
    assertFalse(painter.hasPaint());
  }

  @Test
  void largeGridNoInfinitePaintWhenPaintSpecified() {
    World w = new World(20);
    World.setInstance(w);
    Painter painter = new Painter(0, 0, "North", 1);
    assertTrue(painter.hasPaint());
    painter.paint("red");
    assertFalse(painter.hasPaint());
  }

  @Test
  void noInfinitePaint() {
    World w = new World(19);
    World.setInstance(w);
    Painter painter = new Painter(0, 0, "North", 1);
    assertTrue(painter.hasPaint());
    painter.paint("red");
    assertFalse(painter.hasPaint());
  }
}
