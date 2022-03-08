package org.code.validation.support;

import org.code.neighborhood.NeighborhoodSignalKey;
import org.code.validation.NeighborhoodActionType;

/**
 * Support class which maps NeighborhoodSignalKeys to NeighborhoodActionTypes. This allows us to
 * change NeighborhoodSignalKeys without needing to update the validation api.
 */
public class NeighborhoodActionTypeMapper {
  /**
   * @param signalKey
   * @return NeighborhoodActionType which is equivalent to the given signalKey
   */
  public static NeighborhoodActionType convertNeighborhoodKeyToActionType(
      NeighborhoodSignalKey signalKey) {
    switch (signalKey) {
      case INITIALIZE_PAINTER:
        return NeighborhoodActionType.INITIALIZE_PAINTER;
      case MOVE:
        return NeighborhoodActionType.MOVE;
      case PAINT:
        return NeighborhoodActionType.PAINT;
      case REMOVE_PAINT:
        return NeighborhoodActionType.REMOVE_PAINT;
      case TAKE_PAINT:
        return NeighborhoodActionType.TAKE_PAINT;
      case HIDE_PAINTER:
        return NeighborhoodActionType.HIDE_PAINTER;
      case SHOW_PAINTER:
        return NeighborhoodActionType.SHOW_PAINTER;
      case TURN_LEFT:
        return NeighborhoodActionType.TURN_LEFT;
      case HIDE_BUCKETS:
        return NeighborhoodActionType.HIDE_BUCKETS;
      case SHOW_BUCKETS:
        return NeighborhoodActionType.SHOW_BUCKETS;
      default:
        return null;
    }
  }
}
