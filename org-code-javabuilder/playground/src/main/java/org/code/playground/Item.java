package org.code.playground;

import java.util.HashMap;
import java.util.UUID;
import org.code.protocol.ClientMessageDetailKeys;

public abstract class Item {
  private int xLocation;
  private int yLocation;
  private int height;
  private final String id;
  private final PlaygroundMessageHandler playgroundMessageHandler;
  private boolean shouldSendMessages;

  Item(int x, int y, int height) {
    this.xLocation = x;
    this.yLocation = y;
    this.height = height;
    this.id = UUID.randomUUID().toString();
    this.playgroundMessageHandler = PlaygroundMessageHandler.getInstance();
    this.shouldSendMessages = false;
  }

  /**
   * Set the X position for the item.
   *
   * @param x the distance, in pixels, from the left side of the board
   */
  public void setX(int x) {
    this.xLocation = x;
    this.sendChangeMessage(ClientMessageDetailKeys.X, Integer.toString(x));
  }

  /**
   * Get the X position for the item.
   *
   * @return the distance, in pixels, from the left side of the board
   */
  public int getX() {
    return this.xLocation;
  }

  /**
   * Set the Y position for the item.
   *
   * @param y the distance, in pixels, from the top of the board
   */
  public void setY(int y) {
    this.yLocation = y;
    this.sendChangeMessage(ClientMessageDetailKeys.Y, Integer.toString(y));
  }

  /**
   * Get the Y position for the item.
   *
   * @return the distance, in pixels, from the top of the board
   */
  public int getY() {
    return this.yLocation;
  }

  /**
   * Set the height for the item.
   *
   * @param height the height of the item, in pixels
   */
  public void setHeight(int height) {
    this.height = height;
    this.sendChangeMessage(ClientMessageDetailKeys.HEIGHT, Integer.toString(height));
  }

  /**
   * Get the height for the item.
   *
   * @return the height of the item, in pixels
   */
  public int getHeight() {
    return this.height;
  }

  protected String getId() {
    return this.id;
  }

  protected HashMap<String, String> getDetails() {
    HashMap<String, String> details = this.getIdDetails();
    details.put(ClientMessageDetailKeys.HEIGHT, Integer.toString(this.getHeight()));
    details.put(ClientMessageDetailKeys.X, Integer.toString(this.getX()));
    details.put(ClientMessageDetailKeys.Y, Integer.toString(this.getY()));
    return details;
  }

  protected HashMap<String, String> getRemoveDetails() {
    return this.getIdDetails();
  }

  protected void sendChangeMessage(String key, String value) {
    if (this.shouldSendMessages) {
      HashMap<String, String> details = this.getIdDetails();
      details.put(key, value);
      this.playgroundMessageHandler.sendMessage(
          new PlaygroundMessage(PlaygroundSignalKey.CHANGE_ITEM, details));
    }
  }

  protected void sendChangeMessage(HashMap<String, String> changeDetails) {
    if (this.shouldSendMessages) {
      changeDetails.put(ClientMessageDetailKeys.ID, this.getId());
      this.playgroundMessageHandler.sendMessage(
          new PlaygroundMessage(PlaygroundSignalKey.CHANGE_ITEM, changeDetails));
    }
  }

  protected void turnOnChangeMessages() {
    this.shouldSendMessages = true;
  }

  protected void turnOffChangeMessages() {
    this.shouldSendMessages = false;
  }

  private HashMap<String, String> getIdDetails() {
    HashMap<String, String> idDetails = new HashMap<>();
    idDetails.put(ClientMessageDetailKeys.ID, this.getId());
    return idDetails;
  }
}
