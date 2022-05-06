package org.code.validation.support;

import static org.code.protocol.ClientMessageDetailKeys.DIRECTION;

import java.util.ArrayList;
import java.util.List;
import org.code.neighborhood.support.Direction;
import org.code.validation.PainterEvent;
import org.code.validation.PainterLog;
import org.code.validation.Position;

/** Support class which tracks a single painter's actions during a run of the neighborhood. */
public class PainterTracker {
  private final String painterId;
  private final Position startingPosition;
  private final int startingPaintCount;
  private final List<PainterEvent> events;

  private Position currentPosition;
  private int currentPaintCount;

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
    this.events.add(event);
    switch (event.getEventType()) {
      case MOVE:
        final String directionString = event.getDetails().get(DIRECTION);
        if (directionString != null) {
          final int currentX = this.currentPosition.getX();
          final int currentY = this.currentPosition.getY();
          switch (Direction.fromString(directionString)) {
            case NORTH:
              this.currentPosition = new Position(currentX, currentY - 1);
              break;
            case EAST:
              this.currentPosition = new Position(currentX + 1, currentY);
              break;
            case SOUTH:
              this.currentPosition = new Position(currentX, currentY + 1);
              break;
            case WEST:
              this.currentPosition = new Position(currentX - 1, currentY);
              break;
          }
        }
        break;
      case PAINT:
        this.currentPaintCount--;
        break;
      case TAKE_PAINT:
        this.currentPaintCount++;
        break;
      default:
        break;
    }
  }

  public Position getCurrentPosition() {
    return this.currentPosition;
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
