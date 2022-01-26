package org.code.validation;

public class PainterLog {
  private String painterId;
  private PainterEvent[] events;

  public boolean didActionOnce(EventType eventType) {
    return false;
  }

  public boolean didActionExactly(EventType eventType, int times) {
    return false;
  }
}
