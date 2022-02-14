package org.code.validation.support;

import static org.code.protocol.ClientMessageDetailKeys.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.code.neighborhood.NeighborhoodSignalKey;
import org.code.neighborhood.World;
import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;
import org.code.validation.NeighborhoodLog;
import org.code.validation.PainterEvent;
import org.code.validation.PainterLog;
import org.code.validation.Position;

public class NeighborhoodTracker {
  private final Map<String, PainterTracker> painterTrackers;
  private String[][] neighborhoodState;
  private boolean isInitialized;

  public NeighborhoodTracker() {
    this.painterTrackers = new HashMap<>();
    this.isInitialized = false;
  }

  public NeighborhoodLog getNeighborhoodLog() {
    final ArrayList<PainterTracker> trackers = new ArrayList<>(painterTrackers.values());
    final PainterLog[] painterLogs = new PainterLog[trackers.size()];
    for (int i = 0; i < painterLogs.length; i++) {
      painterLogs[i] = trackers.get(i).getPainterLog();
    }
    return new NeighborhoodLog(painterLogs, this.neighborhoodState);
  }

  public void trackEvent(ClientMessage message) {
    if (message.getType() != ClientMessageType.NEIGHBORHOOD) {
      return;
    }

    final String id = message.getDetail().getString(ID);
    final NeighborhoodSignalKey key = NeighborhoodSignalKey.valueOf(message.getValue());
    if (key == NeighborhoodSignalKey.INITIALIZE_PAINTER) {
      if (!this.isInitialized) {
        this.initializeGrid();
      }
      final int x = message.getDetail().getInt(X);
      final int y = message.getDetail().getInt(Y);
      final int paint = message.getDetail().getInt(PAINT);
      final PainterTracker painterTracker = new PainterTracker(id, new Position(x, y), paint);
      this.painterTrackers.put(id, painterTracker);
      return;
    }

    if (!this.painterTrackers.containsKey(id) || !this.isInitialized) {
      System.out.printf(
          "Received a painter event for an uninitialized painter. Ignoring message: %s, %s\n",
          message.getValue(), message.getDetail());
      return;
    }

    final PainterTracker tracker = this.painterTrackers.get(id);
    tracker.trackEvent(this.convertToPainterEvent(message));
    final Position position = tracker.getCurrentPosition();

    switch (key) {
      case PAINT:
        this.neighborhoodState[position.getX()][position.getY()] =
            message.getDetail().getString(COLOR);
        break;
      case REMOVE_PAINT:
        this.neighborhoodState[position.getX()][position.getY()] = null;
        break;
      default:
        break;
    }
  }

  private void initializeGrid() {
    final int gridSize = World.getInstance().getGrid().getSize();
    this.neighborhoodState = new String[gridSize][gridSize];
    this.isInitialized = true;
  }

  private PainterEvent convertToPainterEvent(ClientMessage message) {
    final NeighborhoodSignalKey signalKey = NeighborhoodSignalKey.valueOf(message.getValue());
    final Map<String, String> detailsMap =
        message
            .getDetail()
            .keySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Function.identity(), jsonKey -> message.getDetail().getString(jsonKey)));
    return new PainterEvent(
        NeighborhoodActionTypeMapper.convertNeighborhoodKeyToActionType(signalKey), detailsMap);
  }
}
