package org.code.validation.support;

import static org.code.protocol.ClientMessageDetailKeys.DIRECTION;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.code.neighborhood.support.Direction;
import org.code.validation.NeighborhoodActionType;
import org.code.validation.PainterEvent;
import org.code.validation.PainterLog;
import org.code.validation.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PainterTrackerTest {
  private static final String ID = "id";
  private static final Position POSITION = new Position(1, 1);
  private static final int PAINT_COUNT = 10;

  private PainterTracker unitUnderTest;

  @BeforeEach
  public void setUp() {
    unitUnderTest = new PainterTracker(ID, POSITION, PAINT_COUNT);
  }

  @Test
  public void testMoveEventUpdatesPosition() {
    unitUnderTest.trackEvent(createMoveEvent(Direction.EAST));
    // Should move to (2, 1)
    assertEquals(2, unitUnderTest.getCurrentPosition().getX());
    assertEquals(1, unitUnderTest.getCurrentPosition().getY());

    unitUnderTest.trackEvent(createMoveEvent(Direction.SOUTH));
    // Should move to (2, 2)
    assertEquals(2, unitUnderTest.getCurrentPosition().getX());
    assertEquals(2, unitUnderTest.getCurrentPosition().getY());

    unitUnderTest.trackEvent(createMoveEvent(Direction.WEST));
    // Should move to (1, 2)
    assertEquals(1, unitUnderTest.getCurrentPosition().getX());
    assertEquals(2, unitUnderTest.getCurrentPosition().getY());

    unitUnderTest.trackEvent(createMoveEvent(Direction.NORTH));
    // Should move to (1, 1)
    assertEquals(1, unitUnderTest.getCurrentPosition().getX());
    assertEquals(1, unitUnderTest.getCurrentPosition().getY());
  }

  @Test
  public void testPaintEventDecrementsPaintCount() {
    final PainterEvent paintEvent = new PainterEvent(NeighborhoodActionType.PAINT, Map.of());
    unitUnderTest.trackEvent(paintEvent);
    assertEquals(PAINT_COUNT - 1, unitUnderTest.getPainterLog().getEndingPaintCount());
  }

  @Test
  public void testTakePaintEventIncreasesPaintCount() {
    final PainterEvent takePaintEvent =
        new PainterEvent(NeighborhoodActionType.TAKE_PAINT, Map.of());
    unitUnderTest.trackEvent(takePaintEvent);
    assertEquals(PAINT_COUNT + 1, unitUnderTest.getPainterLog().getEndingPaintCount());
  }

  @Test
  public void testGetPainterLogContainsAllData() {
    final PainterEvent event1 = createMoveEvent(Direction.SOUTH);
    final PainterEvent event2 = createMoveEvent(Direction.SOUTH);
    final PainterEvent event3 = createMoveEvent(Direction.EAST);
    final PainterEvent event4 = new PainterEvent(NeighborhoodActionType.PAINT, Map.of());
    final PainterEvent event5 = new PainterEvent(NeighborhoodActionType.PAINT, Map.of());
    final PainterEvent event6 = new PainterEvent(NeighborhoodActionType.TAKE_PAINT, Map.of());
    final PainterEvent event7 = new PainterEvent(NeighborhoodActionType.HIDE_PAINTER, Map.of());
    final List<PainterEvent> events =
        List.of(event1, event2, event3, event4, event5, event6, event7);

    for (PainterEvent event : events) {
      unitUnderTest.trackEvent(event);
    }

    final PainterLog log = unitUnderTest.getPainterLog();

    assertEquals(ID, log.getPainterId());
    assertEquals(POSITION, log.getStartingPosition());
    assertEquals(PAINT_COUNT, log.getStartingPaintCount());
    assertEquals(2, log.getEndingPosition().getX());
    assertEquals(3, log.getEndingPosition().getY());
    assertEquals(PAINT_COUNT - 1, log.getEndingPaintCount());
    assertEquals(events, log.getEvents());
  }

  private PainterEvent createMoveEvent(Direction direction) {
    return new PainterEvent(
        NeighborhoodActionType.MOVE, Map.of(DIRECTION, direction.getDirectionString()));
  }
}
