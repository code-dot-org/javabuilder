package org.code.neighborhood;

import java.util.HashMap;

public class Painter {
  private static int lastId = 0;
  private int xLocation;
  private int yLocation;
  private Direction direction;
  private int remainingPaint;
  private Grid grid;
  private String id;

  /** Creates a Painter object at (0, 0), facing East, with no paint. */
  public Painter() {
    this.xLocation = 0;
    this.yLocation = 0;
    this.direction = Direction.EAST;
    this.remainingPaint = 0;
    this.grid = World.getInstance().getGrid();
    this.id = "painter-" + lastId++;
    this.sendInitializationMessage();
  }

  /**
   * Creates a Painter object
   *
   * @param x the x location of the painter on the grid
   * @param y the y location of the painter on the grid
   * @param direction the direction the painter is facing
   * @param paint the amount of paint the painter has to start
   */
  public Painter(int x, int y, String direction, int paint) {
    this.xLocation = x;
    this.yLocation = y;
    this.direction = Direction.fromString(direction);
    this.remainingPaint = paint;
    this.grid = World.getInstance().getGrid();
    int gridSize = this.grid.getSize();
    if (x < 0 || y < 0 || x >= gridSize || y >= gridSize) {
      throw new UnsupportedOperationException(ExceptionKeys.INVALID_LOCATION.toString());
    }
    this.id = "painter-" + lastId++;
    this.sendInitializationMessage();
  }

  /** Turns the painter one compass direction left (i.e. North -> West). */
  public void turnLeft() {
    this.direction = this.direction.turnLeft();
    HashMap<String, String> details = this.getSignalDetails();
    details.put("direction", this.direction.getDirectionString());
    this.sendOutputMessage(NeighborhoodSignalKey.TURN_LEFT, details);
  }

  /** Move the painter one square forward in the direction the painter is facing. */
  public void move() {
    if (this.isValidMovement(this.direction)) {
      if (this.direction.isNorth()) {
        this.yLocation--;
      } else if (this.direction.isSouth()) {
        this.yLocation++;
      } else if (this.direction.isEast()) {
        this.xLocation++;
      } else {
        this.xLocation--;
      }
    } else {
      throw new UnsupportedOperationException(ExceptionKeys.INVALID_MOVE.toString());
    }
    HashMap<String, String> details = this.getSignalDetails();
    details.put("direction", this.direction.getDirectionString());
    this.sendOutputMessage(NeighborhoodSignalKey.MOVE, details);
  }

  /**
   * Add paint to the grid at the painter's location.
   *
   * @param color the color of the paint being added
   */
  public void paint(String color) {
    if (this.remainingPaint > 0) {
      this.grid.getSquare(this.xLocation, this.yLocation).setColor(color);
      this.remainingPaint--;
      HashMap<String, String> details = this.getSignalDetails();
      details.put("color", color);
      this.sendOutputMessage(NeighborhoodSignalKey.PAINT, details);
    } else {
      System.out.println("There is no more paint in the painter's bucket");
    }
  }

  /** Removes all paint on the square where the painter is standing. */
  public void scrapePaint() {
    this.grid.getSquare(this.xLocation, this.yLocation).removePaint();
    this.sendOutputMessage(NeighborhoodSignalKey.REMOVE_PAINT, this.getSignalDetails());
  }

  /**
   * Returns how many units of paint are in the painter's personal bucket.
   *
   * @return the units of paint in the painter's bucket
   */
  public int getMyPaint() {
    return this.remainingPaint;
  }

  /** Hides the painter on the screen. */
  public void hidePainter() {
    this.sendOutputMessage(NeighborhoodSignalKey.HIDE_PAINTER, this.getSignalDetails());
  }

  /** Shows the painter on the screen. */
  public void showPainter() {
    this.sendOutputMessage(NeighborhoodSignalKey.SHOW_PAINTER, this.getSignalDetails());
  }

  /**
   * The Painter adds a single unit of paint to their personal bucket. The counter on the bucket on
   * the screen goes down. If the painter is not standing on a paint bucket, nothing happens.
   */
  public void takePaint() {
    if (this.grid.getSquare(this.xLocation, this.yLocation).containsPaint()) {
      this.grid.getSquare(this.xLocation, this.yLocation).collectPaint();
      this.remainingPaint++;
      this.sendOutputMessage(NeighborhoodSignalKey.TAKE_PAINT, this.getSignalDetails());
    } else {
      System.out.println("There is no paint to collect here");
    }
  }

  /** @return True if there is paint in the square where the painter is standing. */
  public boolean isOnPaint() {
    return this.grid.getSquare(this.xLocation, this.yLocation).hasColor();
  }

  /** @return True if there is a paint bucket in the square where the painter is standing. */
  public boolean isOnBucket() {
    return this.grid.getSquare(this.xLocation, this.yLocation).containsPaint();
  }

  /** @return True if the painter's personal bucket has paint in it. */
  public boolean hasPaint() {
    return this.remainingPaint > 0;
  }

  /** @return True if there is no barrier one square ahead in the requested direction. */
  public boolean canMove(String direction) {
    return this.isValidMovement(Direction.fromString(direction));
  }

  /** @return the color of the square where the painter is standing. */
  public String getColor() {
    return this.grid.getSquare(this.xLocation, this.yLocation).getColor();
  }

  /** @return True if facing North */
  public boolean facingNorth() {
    return this.direction.isNorth();
  }

  /** @return True if facing East */
  public boolean facingEast() {
    return this.direction.isEast();
  }

  /** @return True if facing South */
  public boolean facingSouth() {
    return this.direction.isSouth();
  }

  /** @return True if facing West */
  public boolean facingWest() {
    return this.direction.isWest();
  }

  /**
   * Helper function to check if the painter can move in the specified direction.
   *
   * @param movementDirection the direction of movement
   * @return True if the painter can move in that direction
   */
  private boolean isValidMovement(Direction movementDirection) {
    if (movementDirection.isNorth()) {
      return this.grid.validLocation(this.xLocation, this.yLocation - 1);
    } else if (movementDirection.isSouth()) {
      return this.grid.validLocation(this.xLocation, this.yLocation + 1);
    } else if (movementDirection.isEast()) {
      return this.grid.validLocation(this.xLocation + 1, this.yLocation);
    } else {
      return this.grid.validLocation(this.xLocation - 1, this.yLocation);
    }
  }

  private HashMap<String, String> getSignalDetails() {
    HashMap<String, String> details = new HashMap<>();
    details.put("id", this.id);
    return details;
  }

  private void sendOutputMessage(NeighborhoodSignalKey signalKey, HashMap<String, String> details) {
    NeighborhoodOutputHandler.sendMessage(new NeighborhoodSignalMessage(signalKey, details));
  }

  private void sendInitializationMessage() {
    HashMap<String, String> initDetails = this.getSignalDetails();
    initDetails.put("direction", this.direction.getDirectionString());
    initDetails.put("x", Integer.toString(this.xLocation));
    initDetails.put("y", Integer.toString(this.yLocation));
    this.sendOutputMessage(NeighborhoodSignalKey.INITIALIZE_PAINTER, initDetails);
  }
}
