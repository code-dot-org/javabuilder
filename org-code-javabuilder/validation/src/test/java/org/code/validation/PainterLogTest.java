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
    sampleEvents.add(new PainterEvent(EventType.INITIALIZE_PAINTER, null));
    sampleEvents.add(new PainterEvent(EventType.TURN_LEFT, null));
    sampleEvents.add(new PainterEvent(EventType.MOVE, null));
    sampleEvents.add(new PainterEvent(EventType.TURN_LEFT, null));
    sampleEvents.add(new PainterEvent(EventType.MOVE, null));
    sampleEvents.add(new PainterEvent(EventType.PAINT, null));
    sampleEvents.add(new PainterEvent(EventType.PAINT, null));
    sampleEvents.add(new PainterEvent(EventType.PAINT, null));
    sampleEvents.add(new PainterEvent(EventType.TAKE_PAINT, null));
    unitUnderTest =
        new PainterLog("sampleId", new Position(0, 0), new Position(5, 5), 0, 5, sampleEvents);
  }

  @Test
  public void didActionOnceReturnsCorrectly() {
    assertTrue(unitUnderTest.didActionOnce(EventType.TAKE_PAINT));
    assertFalse(unitUnderTest.didActionOnce(EventType.TURN_LEFT));
    assertFalse(unitUnderTest.didActionOnce(EventType.HIDE_BUCKETS));
  }

  @Test
  public void didActionExactlyReturnsCorrectly() {
    assertTrue(unitUnderTest.didActionExactly(EventType.TAKE_PAINT, 1));
    assertTrue(unitUnderTest.didActionExactly(EventType.TURN_LEFT, 2));
    assertFalse(unitUnderTest.didActionExactly(EventType.HIDE_BUCKETS, 3));
    assertFalse(unitUnderTest.didActionExactly(EventType.PAINT, 2));
  }

  @Test
  public void didActionAtLeastReturnsCorrectly() {
    assertTrue(unitUnderTest.didActionAtLeast(EventType.TAKE_PAINT, 1));
    assertTrue(unitUnderTest.didActionAtLeast(EventType.TURN_LEFT, 2));
    assertFalse(unitUnderTest.didActionAtLeast(EventType.HIDE_BUCKETS, 3));
    assertTrue(unitUnderTest.didActionAtLeast(EventType.PAINT, 2));
    assertFalse(unitUnderTest.didActionAtLeast(EventType.PAINT, 4));
  }
}
