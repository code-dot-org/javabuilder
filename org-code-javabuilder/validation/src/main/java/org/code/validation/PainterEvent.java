package org.code.validation;

import java.util.Map;

/** User-facing class representing a single action for a single Painter. */
public class PainterEvent {
  private final NeighborhoodActionType neighborhoodActionType;
  private final Map<String, String> details;

  public PainterEvent(NeighborhoodActionType neighborhoodActionType, Map<String, String> details) {
    this.neighborhoodActionType = neighborhoodActionType;
    this.details = details;
  }

  public NeighborhoodActionType getEventType() {
    return this.neighborhoodActionType;
  }

  public Map<String, String> getDetails() {
    return this.details;
  }
}
