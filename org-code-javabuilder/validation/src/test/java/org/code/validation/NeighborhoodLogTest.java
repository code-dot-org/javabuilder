package org.code.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NeighborhoodLogTest {
  private NeighborhoodLog unitUnderTest;

  @BeforeEach
  public void setup() {
    List<PainterEvent> eventList1 = new ArrayList<>();
    eventList1.add(new PainterEvent(EventType.INITIALIZE_PAINTER, null));
    eventList1.add(new PainterEvent(EventType.TURN_LEFT, null));
    eventList1.add(new PainterEvent(EventType.MOVE, null));
    eventList1.add(new PainterEvent(EventType.MOVE, null));

    List<PainterEvent> eventList2 = new ArrayList<>();
    eventList2.add(new PainterEvent(EventType.INITIALIZE_PAINTER, null));
    eventList2.add(new PainterEvent(EventType.TAKE_PAINT, null));
    eventList2.add(new PainterEvent(EventType.TURN_LEFT, null));
    eventList2.add(new PainterEvent(EventType.TURN_LEFT, null));
    eventList2.add(new PainterEvent(EventType.PAINT, null));
    eventList2.add(new PainterEvent(EventType.MOVE, null));
    eventList2.add(new PainterEvent(EventType.PAINT, null));

    PainterLog[] painterLogs =
        new PainterLog[] {this.createPainterLog(eventList1), this.createPainterLog(eventList2)};
    this.unitUnderTest = new NeighborhoodLog(painterLogs, this.getSampleFinalOutput());
  }

  @Test
  public void onePainterDidActionReturnsCorrectly() {
    assertTrue(unitUnderTest.onePainterDidAction(EventType.TURN_LEFT, 2));
    assertFalse(unitUnderTest.onePainterDidAction(EventType.HIDE_PAINTER, 1));
    assertFalse(unitUnderTest.onePainterDidAction(EventType.MOVE, 3));
  }

  @Test
  public void actionHappenedReturnsCorrectly() {
    assertTrue(unitUnderTest.actionHappened(EventType.TURN_LEFT, 3));
    assertFalse(unitUnderTest.actionHappened(EventType.HIDE_PAINTER, 1));
    assertFalse(unitUnderTest.actionHappened(EventType.MOVE, 2));
  }

  @Test
  public void outputMatchesReturnsCorrectlyForValidOutput() {
    String[][] expectedOutput = new String[2][2];
    expectedOutput[0] = new String[] {"red", null};
    expectedOutput[1] = new String[] {null, "green"};
    assertTrue(unitUnderTest.finalOutputMatches(expectedOutput));
  }

  @Test
  public void outputMatchesReturnsCorrectlyForInvalidOutput() {
    String[][] expectedOutput = new String[2][2];
    expectedOutput[0] = new String[] {null, "blue"};
    expectedOutput[1] = new String[] {null, "green"};
    assertFalse(unitUnderTest.finalOutputMatches(expectedOutput));
  }

  @Test
  public void finalOutputContainsPaintReturnsCorrectlyForValidOutput() {
    boolean[][] expectedPaint = new boolean[2][2];
    expectedPaint[0] = new boolean[] {true, false};
    expectedPaint[1] = new boolean[] {false, true};
    assertTrue(unitUnderTest.finalOutputContainsPaint(expectedPaint));
  }

  @Test
  public void finalOutputContainsPaintReturnsCorrectlyForInvalidOutput() {
    boolean[][] expectedPaint = new boolean[2][2];
    expectedPaint[0] = new boolean[] {true, true};
    expectedPaint[1] = new boolean[] {false, true};
    assertFalse(unitUnderTest.finalOutputContainsPaint(expectedPaint));
  }

  private PainterLog createPainterLog(List<PainterEvent> events) {
    return new PainterLog("sampleId", new Position(0, 0), new Position(5, 5), 0, 5, events);
  }

  private String[][] getSampleFinalOutput() {
    String[][] output = new String[2][2];
    output[0] = new String[] {"red", null};
    output[1] = new String[] {null, "green"};
    return output;
  }
}
