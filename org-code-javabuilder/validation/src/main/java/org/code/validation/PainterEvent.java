package org.code.validation;

import java.util.Map;

public class PainterEvent {
  private final EventType eventType;
  private final Map<String, String> details;

  public PainterEvent(EventType eventType, Map<String, String> details) {
    this.eventType = eventType;
    this.details = details;
  }

  public EventType getEventType() {
    return this.eventType;
  }

  public Map<String, String> getDetails() {
    return this.details;
  }
}
