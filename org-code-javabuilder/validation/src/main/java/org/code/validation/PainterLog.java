package org.code.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User-facing class representing all the actions a single painter took during a run of the
 * neighborhood. Includes helpers for anlayzing which actions the painter did.
 */
public class PainterLog {
  private final String painterId;
  private final List<PainterEvent> events;
  private final Map<NeighborhoodActionType, Integer> eventCounts;
  private final Position startingPosition;
  private final Position endingPosition;
  private final int startingPaintCount;
  private final int endingPaintCount;

  public PainterLog(
      String painterId,
      Position startingPosition,
      Position endingPosition,
      int startingPaintCount,
      int endingPaintCount,
      List<PainterEvent> events) {
    this.painterId = painterId;
    this.events = events;
    this.eventCounts = this.getEventCounts(events);
    this.startingPaintCount = startingPaintCount;
    this.endingPaintCount = endingPaintCount;
    this.startingPosition = startingPosition;
    this.endingPosition = endingPosition;
  }

  /**
   * @param neighborhoodActionType
   * @return true if the painter did the given action exactly once, false otherwise
   */
  public boolean didActionOnce(NeighborhoodActionType neighborhoodActionType) {
    return this.didActionExactly(neighborhoodActionType, 1);
  }

  /**
   * @param neighborhoodActionType
   * @param times
   * @return true if the painter did the given action exactly "times" times, false otherwise
   */
  public boolean didActionExactly(NeighborhoodActionType neighborhoodActionType, int times) {
    if (this.eventCounts.containsKey(neighborhoodActionType)) {
      return eventCounts.get(neighborhoodActionType) == times;
    }
    return false;
  }

  /**
   * @param neighborhoodActionType
   * @param times
   * @return true if the painter did the given action at least "times" times, false otherwise
   */
  public boolean didActionAtLeast(NeighborhoodActionType neighborhoodActionType, int times) {
    if (this.eventCounts.containsKey(neighborhoodActionType)) {
      return eventCounts.get(neighborhoodActionType) >= times;
    }
    return false;
  }

  /**
   * @param neighborhoodActionType
   * @return the number of times the painter did the given neighborhoodActionType.
   */
  public int actionCount(NeighborhoodActionType neighborhoodActionType) {
    if (this.eventCounts.containsKey(neighborhoodActionType)) {
      return eventCounts.get(neighborhoodActionType);
    }
    return 0;
  }

  public Position getStartingPosition() {
    return this.startingPosition;
  }

  public Position getEndingPosition() {
    return this.endingPosition;
  }

  public int getStartingPaintCount() {
    return this.startingPaintCount;
  }

  public int getEndingPaintCount() {
    return this.endingPaintCount;
  }

  public String getPainterId() {
    return this.painterId;
  }

  public List<PainterEvent> getEvents() {
    return this.events;
  }

  private Map<NeighborhoodActionType, Integer> getEventCounts(List<PainterEvent> events) {
    Map<NeighborhoodActionType, Integer> eventCountMap = new HashMap<>();
    for (PainterEvent event : events) {
      NeighborhoodActionType neighborhoodActionType = event.getEventType();
      if (!eventCountMap.containsKey(neighborhoodActionType)) {
        eventCountMap.put(neighborhoodActionType, 0);
      }
      eventCountMap.put(neighborhoodActionType, eventCountMap.get(neighborhoodActionType) + 1);
    }
    return eventCountMap;
  }
}
