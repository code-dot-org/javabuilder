package org.code.playground;

public enum PlaygroundSignalKey {
  // Indicate that the Playground game has started
  RUN,
  // Indicate that the Playground game has ended
  EXIT,
  // Add an image item to the Playground
  ADD_IMAGE_ITEM,
  // Add a clickable item to the Playground
  ADD_CLICKABLE_ITEM,
  // Add a text item to the Playground
  ADD_TEXT_ITEM,
  // Remove an item from the Playground
  REMOVE_ITEM,
  // Change an item's properties
  CHANGE_ITEM,
  // Play a sound
  PLAY_SOUND,
  // Set the background image of the Playground
  SET_BACKGROUND_IMAGE,
  // Indicate that the current update cycle has completed
  UPDATE_COMPLETE
}
