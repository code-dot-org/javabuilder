package org.code.validation;

import org.code.neighborhood.NeighborhoodSignalKey;

public class EventTypeMapper {
  // Map NeighborhoodSignalKey to EventType. This allows us to change NeighborhoodSignalKeys without
  // needing to update the validation api.
  public EventType convertNeighborhoodKeyToEventType(NeighborhoodSignalKey signalKey) {
    switch (signalKey) {
      case INITIALIZE_PAINTER:
        return EventType.INITIALIZE_PAINTER;
      case MOVE:
        return EventType.MOVE;
      case PAINT:
        return EventType.PAINT;
      case REMOVE_PAINT:
        return EventType.REMOVE_PAINT;
      case TAKE_PAINT:
        return EventType.TAKE_PAINT;
      case HIDE_PAINTER:
        return EventType.HIDE_PAINTER;
      case SHOW_PAINTER:
        return EventType.SHOW_PAINTER;
      case TURN_LEFT:
        return EventType.TURN_LEFT;
      case HIDE_BUCKETS:
        return EventType.HIDE_BUCKETS;
      case SHOW_BUCKETS:
        return EventType.SHOW_BUCKETS;
      default:
        return null;
    }
  }
}
