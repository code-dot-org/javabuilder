package org.code.validation.support;

import static org.code.protocol.ClientMessageDetailKeys.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import org.code.neighborhood.*;
import org.code.protocol.ClientMessageType;
import org.code.protocol.GlobalProtocolTestFactory;
import org.code.validation.ClientMessageHelper;
import org.code.validation.PainterLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NeighborhoodTrackerTest {
  private static final int GRID_SIZE = 20;
  private NeighborhoodTracker unitUnderTest;

  @BeforeEach
  public void setUp() {
    GlobalProtocolTestFactory.builder().create();
    World.setInstance(new World(GRID_SIZE));
    unitUnderTest = new NeighborhoodTracker();
  }

  @AfterEach
  public void tearDown() {
    GlobalProtocolTestFactory.tearDown();
  }

  @Test
  public void testIgnoresNonNeighborhoodMessages() {
    unitUnderTest.trackEvent(new ClientMessageHelper(ClientMessageType.STATUS, ""));
    assertEquals(0, unitUnderTest.getNeighborhoodLog().getPainterLogs().length);
  }

  @Test
  public void testInitializeMessageCreatesNewPainterTracker() {
    unitUnderTest.trackEvent(createInitEvent("id", Direction.EAST, 5, 10, 20));

    final PainterLog painterLog = unitUnderTest.getNeighborhoodLog().getPainterLogs()[0];
    assertEquals("id", painterLog.getPainterId());
    assertEquals(5, painterLog.getStartingPosition().getX());
    assertEquals(10, painterLog.getStartingPosition().getY());
    assertEquals(20, painterLog.getStartingPaintCount());
  }

  @Test
  public void testUpdatesPainterTrackerWithPainterMessages() {
    final String id = "id";
    // Initialize
    unitUnderTest.trackEvent(createInitEvent(id, Direction.EAST, 5, 10, 20));

    final HashMap<String, String> moveDetails = new HashMap<>();
    moveDetails.put(DIRECTION, Direction.WEST.getDirectionString());
    moveDetails.put(ID, id);
    // Move west (5, 10) -> (4, 10)
    unitUnderTest.trackEvent(
        new NeighborhoodSignalMessage(NeighborhoodSignalKey.MOVE, moveDetails));

    final HashMap<String, String> paintDetails = new HashMap<>();
    paintDetails.put(ID, id);
    // Take paint
    unitUnderTest.trackEvent(
        new NeighborhoodSignalMessage(NeighborhoodSignalKey.TAKE_PAINT, paintDetails));

    final PainterLog painterLog = unitUnderTest.getNeighborhoodLog().getPainterLogs()[0];

    assertEquals(ID, painterLog.getPainterId());
    assertEquals(4, painterLog.getEndingPosition().getX());
    assertEquals(10, painterLog.getEndingPosition().getY());
    assertEquals(21, painterLog.getEndingPaintCount());
  }

  @Test
  public void testPaintEventsUpdateGrid() {
    final String id = "id";
    unitUnderTest.trackEvent(createInitEvent(id, Direction.SOUTH, 5, 15, 10));

    final HashMap<String, String> paintDetails = new HashMap<>();
    paintDetails.put(COLOR, "orange");
    paintDetails.put(ID, id);
    unitUnderTest.trackEvent(
        new NeighborhoodSignalMessage(NeighborhoodSignalKey.PAINT, paintDetails));

    assertEquals("orange", unitUnderTest.getNeighborhoodLog().getFinalOutput()[5][15]);

    final HashMap<String, String> removeDetails = new HashMap<>();
    removeDetails.put(ID, id);
    unitUnderTest.trackEvent(
        new NeighborhoodSignalMessage(NeighborhoodSignalKey.REMOVE_PAINT, removeDetails));

    assertNull(unitUnderTest.getNeighborhoodLog().getFinalOutput()[5][15]);
  }

  @Test
  public void testSelectsCorrectPainterTrackerById() {
    final String id1 = "painter1";
    final String id2 = "painter2";
    final String id3 = "painter3";

    unitUnderTest.trackEvent(createInitEvent(id1, Direction.NORTH, 1, 5, 10));
    unitUnderTest.trackEvent(createInitEvent(id2, Direction.EAST, 2, 6, 10));
    unitUnderTest.trackEvent(createInitEvent(id3, Direction.WEST, 3, 7, 10));

    // Painter 2 paints current square ivory
    final HashMap<String, String> paintDetails = new HashMap<>();
    paintDetails.put(ID, id2);
    paintDetails.put(COLOR, "ivory");
    unitUnderTest.trackEvent(
        new NeighborhoodSignalMessage(NeighborhoodSignalKey.PAINT, paintDetails));

    // Painter 3 moves one unit west (3, 7) -> (2, 7)
    final HashMap<String, String> moveDetails = new HashMap<>();
    moveDetails.put(ID, id3);
    moveDetails.put(DIRECTION, Direction.WEST.getDirectionString());
    unitUnderTest.trackEvent(
        new NeighborhoodSignalMessage(NeighborhoodSignalKey.MOVE, moveDetails));

    assertEquals("ivory", unitUnderTest.getNeighborhoodLog().getFinalOutput()[2][6]);

    final PainterLog painter2Log =
        findPainterLogById(unitUnderTest.getNeighborhoodLog().getPainterLogs(), id2);
    assertNotNull(painter2Log);
    assertEquals(9, painter2Log.getEndingPaintCount());

    final PainterLog painter3Log =
        findPainterLogById(unitUnderTest.getNeighborhoodLog().getPainterLogs(), id3);
    assertNotNull(painter3Log);
    assertEquals(2, painter3Log.getEndingPosition().getX());
  }

  private NeighborhoodSignalMessage createInitEvent(
      String id, Direction direction, int x, int y, int paint) {
    final HashMap<String, String> details = new HashMap<>();
    details.put(ID, id);
    details.put(DIRECTION, direction.getDirectionString());
    details.put(X, Integer.toString(x));
    details.put(Y, Integer.toString(y));
    details.put(PAINT, Integer.toString(paint));
    return new NeighborhoodSignalMessage(NeighborhoodSignalKey.INITIALIZE_PAINTER, details);
  }

  private PainterLog findPainterLogById(PainterLog[] painterLogs, String id) {
    for (PainterLog log : painterLogs) {
      if (log.getPainterId().equals(id)) {
        return log;
      }
    }
    return null;
  }
}
