package org.code.playground;

import java.util.HashMap;
import java.util.UUID;

public abstract class Item {
  private int xLocation;
  private int yLocation;
  private int height;
  private final String id;
  private final PlaygroundMessageHandler playgroundMessageHandler;

  private final String HEIGHT_KEY = "height";
  private final String X_KEY = "x";
  private final String Y_KEY = "y";
  private final String ID_KEY = "id";

  Item(int x, int y, int height) {
    this.xLocation = x;
    this.yLocation = y;
    this.height = height;
    this.id = UUID.randomUUID().toString();
    this.playgroundMessageHandler = PlaygroundMessageHandler.getInstance();
  }

  /**
   * Set the X position for the item.
   *
   * @param x the distance, in pixels, from the left side of the board
   */
  public void setX(int x) {
    this.xLocation = x;
    this.sendChangeMessage(X_KEY, Integer.toString(x));
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
    this.sendChangeMessage(Y_KEY, Integer.toString(y));
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
    this.sendChangeMessage(HEIGHT_KEY, Integer.toString(height));
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
    details.put(HEIGHT_KEY, Integer.toString(this.getHeight()));
    details.put(X_KEY, Integer.toString(this.getX()));
    details.put(Y_KEY, Integer.toString(this.getY()));
    return details;
  }

  protected HashMap<String, String> getRemoveDetails() {
    HashMap<String, String> details = this.getIdDetails();
    return details;
  }

  protected void sendChangeMessage(String key, String value) {
    HashMap<String, String> details = this.getIdDetails();
    details.put(key, value);
    this.playgroundMessageHandler.sendMessage(
        new PlaygroundMessage(PlaygroundSignalKey.CHANGE_ITEM, details));
  }

  protected void sendChangeMessage(HashMap<String, String> changeDetails) {
    changeDetails.put(ID_KEY, this.getId());
    this.playgroundMessageHandler.sendMessage(
        new PlaygroundMessage(PlaygroundSignalKey.CHANGE_ITEM, changeDetails));
  }

  private HashMap<String, String> getIdDetails() {
    HashMap<String, String> idDetails = new HashMap<>();
    idDetails.put(ID_KEY, this.getId());
    return idDetails;
  }
}
