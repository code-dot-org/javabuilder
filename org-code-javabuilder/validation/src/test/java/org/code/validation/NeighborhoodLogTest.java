package org.code.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NeighborhoodLogTest {
  private NeighborhoodLog unitUnderTest;

  @BeforeEach
  public void setUp() {
    List<PainterEvent> eventList1 = new ArrayList<>();
    eventList1.add(new PainterEvent(NeighborhoodActionType.INITIALIZE_PAINTER, null));
    eventList1.add(new PainterEvent(NeighborhoodActionType.TURN_LEFT, null));
    eventList1.add(new PainterEvent(NeighborhoodActionType.MOVE, null));
    eventList1.add(new PainterEvent(NeighborhoodActionType.MOVE, null));

    List<PainterEvent> eventList2 = new ArrayList<>();
    eventList2.add(new PainterEvent(NeighborhoodActionType.INITIALIZE_PAINTER, null));
    eventList2.add(new PainterEvent(NeighborhoodActionType.TAKE_PAINT, null));
    eventList2.add(new PainterEvent(NeighborhoodActionType.TURN_LEFT, null));
    eventList2.add(new PainterEvent(NeighborhoodActionType.TURN_LEFT, null));
    eventList2.add(new PainterEvent(NeighborhoodActionType.PAINT, null));
    eventList2.add(new PainterEvent(NeighborhoodActionType.MOVE, null));
    eventList2.add(new PainterEvent(NeighborhoodActionType.PAINT, null));

    PainterLog[] painterLogs =
        new PainterLog[] {this.createPainterLog(eventList1), this.createPainterLog(eventList2)};
    this.unitUnderTest = new NeighborhoodLog(painterLogs, this.getSampleFinalOutput());
  }

  @Test
  public void onePainterDidActionReturnsCorrectly() {
    assertTrue(unitUnderTest.onePainterDidAction(NeighborhoodActionType.TURN_LEFT, 2));
    assertFalse(unitUnderTest.onePainterDidAction(NeighborhoodActionType.HIDE_PAINTER, 1));
    assertFalse(unitUnderTest.onePainterDidAction(NeighborhoodActionType.MOVE, 3));
  }

  @Test
  public void actionHappenedReturnsCorrectly() {
    assertTrue(unitUnderTest.actionHappened(NeighborhoodActionType.TURN_LEFT, 3));
    assertFalse(unitUnderTest.actionHappened(NeighborhoodActionType.HIDE_PAINTER, 1));
    assertFalse(unitUnderTest.actionHappened(NeighborhoodActionType.MOVE, 2));
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
