package org.code.neighborhood.support;

public enum NeighborhoodSignalKey {
  // Initialize a new painter
  INITIALIZE_PAINTER,
  // Move the painter one space forward
  MOVE,
  // Paint the current location
  PAINT,
  // Remove all paint from current location
  REMOVE_PAINT,
  // Take paint from the bucket
  TAKE_PAINT,
  // Hide the painter on the screen
  HIDE_PAINTER,
  // Show the painter on the screen
  SHOW_PAINTER,
  // Turn the painter left
  TURN_LEFT,
  // Hide all paint buckets
  HIDE_BUCKETS,
  // Show all paint buckets
  SHOW_BUCKETS,
  // isOnBucket was called (used for validation only)
  IS_ON_BUCKET,
  // isOnPaint was called (used for validation only)
  IS_ON_PAINT,
  // canMove was called (used for validation only)
  CAN_MOVE
}
