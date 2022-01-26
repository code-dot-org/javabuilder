package org.code.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PainterLog {
  private String painterId;
  private List<PainterEvent> events;
  private Map<EventType, Integer> eventCounts;
  private Position startingPosition;
  private Position endingPosition;
  private int startingPaintCount;
  private int endingPaintCount;

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
   * @param eventType
   * @return true if the painter did the given action exactly once, false otherwise
   */
  public boolean didActionOnce(EventType eventType) {
    return this.didActionExactly(eventType, 1);
  }

  /**
   * @param eventType
   * @param times
   * @return true if the painter did the given action exactly "times" times, false otherwise
   */
  public boolean didActionExactly(EventType eventType, int times) {
    if (this.eventCounts.containsKey(eventType)) {
      return eventCounts.get(eventType) == times;
    }
    return false;
  }

  /**
   * @param eventType
   * @param times
   * @return true if the painter did the given action at least "times" times, false otherwise
   */
  public boolean didActionAtLeast(EventType eventType, int times) {
    if (this.eventCounts.containsKey(eventType)) {
      return eventCounts.get(eventType) >= times;
    }
    return false;
  }

  /**
   * @param eventType
   * @return the number of time the painter did the given eventType.
   */
  public int actionCount(EventType eventType) {
    if (this.eventCounts.containsKey(eventType)) {
      return eventCounts.get(eventType);
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

  private Map<EventType, Integer> getEventCounts(List<PainterEvent> events) {
    Map<EventType, Integer> eventCountMap = new HashMap<>();
    for (PainterEvent event : events) {
      EventType eventType = event.getEventType();
      if (!eventCountMap.containsKey(eventType)) {
        eventCountMap.put(eventType, 0);
      }
      eventCountMap.put(eventType, eventCountMap.get(eventType) + 1);
    }
    return eventCountMap;
  }
}
