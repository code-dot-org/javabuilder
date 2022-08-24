package org.code.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PainterLogTest {
  private List<PainterEvent> sampleEvents;
  private PainterLog unitUnderTest;

  @BeforeEach
  public void setUp() {
    this.sampleEvents = new ArrayList<>();
    sampleEvents.add(new PainterEvent(NeighborhoodActionType.INITIALIZE_PAINTER, null));
    sampleEvents.add(new PainterEvent(NeighborhoodActionType.TURN_LEFT, null));
    sampleEvents.add(new PainterEvent(NeighborhoodActionType.MOVE, null));
    sampleEvents.add(new PainterEvent(NeighborhoodActionType.TURN_LEFT, null));
    sampleEvents.add(new PainterEvent(NeighborhoodActionType.MOVE, null));
    sampleEvents.add(new PainterEvent(NeighborhoodActionType.PAINT, null));
    sampleEvents.add(new PainterEvent(NeighborhoodActionType.PAINT, null));
    sampleEvents.add(new PainterEvent(NeighborhoodActionType.PAINT, null));
    sampleEvents.add(new PainterEvent(NeighborhoodActionType.TAKE_PAINT, null));
    unitUnderTest =
        new PainterLog(
            "sampleId",
            new Position(0, 0, "East"),
            new Position(5, 5, "North"),
            0,
            5,
            sampleEvents);
  }

  @Test
  public void didActionOnceReturnsCorrectly() {
    assertTrue(unitUnderTest.didActionOnce(NeighborhoodActionType.TAKE_PAINT));
    assertFalse(unitUnderTest.didActionOnce(NeighborhoodActionType.TURN_LEFT));
    assertFalse(unitUnderTest.didActionOnce(NeighborhoodActionType.HIDE_BUCKETS));
  }

  @Test
  public void didActionExactlyReturnsCorrectly() {
    assertTrue(unitUnderTest.didActionExactly(NeighborhoodActionType.TAKE_PAINT, 1));
    assertTrue(unitUnderTest.didActionExactly(NeighborhoodActionType.TURN_LEFT, 2));
    assertFalse(unitUnderTest.didActionExactly(NeighborhoodActionType.HIDE_BUCKETS, 3));
    assertFalse(unitUnderTest.didActionExactly(NeighborhoodActionType.PAINT, 2));
  }

  @Test
  public void didActionAtLeastReturnsCorrectly() {
    assertTrue(unitUnderTest.didActionAtLeast(NeighborhoodActionType.TAKE_PAINT, 1));
    assertTrue(unitUnderTest.didActionAtLeast(NeighborhoodActionType.TURN_LEFT, 2));
    assertFalse(unitUnderTest.didActionAtLeast(NeighborhoodActionType.HIDE_BUCKETS, 3));
    assertTrue(unitUnderTest.didActionAtLeast(NeighborhoodActionType.PAINT, 2));
    assertFalse(unitUnderTest.didActionAtLeast(NeighborhoodActionType.PAINT, 4));
  }
}
