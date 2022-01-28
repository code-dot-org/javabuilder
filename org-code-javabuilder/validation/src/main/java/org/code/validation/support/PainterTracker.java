package org.code.validation.support;

import java.util.ArrayList;
import java.util.List;
import org.code.validation.PainterEvent;
import org.code.validation.PainterLog;
import org.code.validation.Position;

/** Support class which tracks a single painter's actions during a run of the neighborhood. */
public class PainterTracker {
  private String painterId;
  private Position startingPosition;
  private Position currentPosition;
  private int startingPaintCount;
  private int currentPaintCount;
  private List<PainterEvent> events;

  public PainterTracker(String painterId, Position position, int paintCount) {
    this.painterId = painterId;
    this.startingPosition = position;
    this.currentPosition = position;
    this.startingPaintCount = paintCount;
    this.currentPaintCount = paintCount;
    this.events = new ArrayList<>();
  }

  // Record the given event, updating position and paint count if necessary.
  public void trackEvent(PainterEvent event) {
    // TODO: fill in method (https://codedotorg.atlassian.net/browse/JAVA-412)
  }

  public PainterLog getPainterLog() {
    return new PainterLog(
        this.painterId,
        this.startingPosition,
        this.currentPosition,
        this.startingPaintCount,
        this.currentPaintCount,
        this.events);
  }
}
