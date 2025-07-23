package org.code.neighborhood;

import static org.code.protocol.ClientMessageDetailKeys.*;

import java.util.HashMap;
import org.code.neighborhood.support.*;
import org.code.protocol.JavabuilderContext;
import org.code.protocol.OutputAdapter;

public class Painter {
  private static final int LARGE_GRID_SIZE = 20;
  private static int lastId = 0;
  private int xLocation;
  private int yLocation;
  private Direction direction;
  private int remainingPaint;
  private final boolean hasInfinitePaint;
  private final Grid grid;
  private final String id;
  private final OutputAdapter outputAdapter;

  /** Creates a Painter object at (0, 0), facing East, with no paint. */
  public Painter() {
    this(0, 0, "East", 0, true);
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
    this(x, y, direction, paint, false);
  }

  private Painter(int x, int y, String direction, int paint, boolean couldHaveInfinitePaint) {
    this.xLocation = x;
    this.yLocation = y;
    this.direction = Direction.fromString(direction);
    this.remainingPaint = paint;
    World currentWorld = (World) JavabuilderContext.getInstance().get(World.class);
    if (currentWorld == null) {
      currentWorld = new World();
      JavabuilderContext.getInstance().register(World.class, currentWorld);
    }
    this.grid = currentWorld.getGrid();
    this.outputAdapter = JavabuilderContext.getInstance().getGlobalProtocol().getOutputAdapter();
    int gridSize = this.grid.getSize();
    this.hasInfinitePaint = couldHaveInfinitePaint ? this.grid.getSize() >= LARGE_GRID_SIZE : false;
    if (x < 0 || y < 0 || x >= gridSize || y >= gridSize) {
      throw new NeighborhoodRuntimeException(ExceptionKeys.INVALID_LOCATION);
    }
    this.id = "painter-" + lastId++;
    this.sendInitializationMessage();
  }

  /** Turns the painter one compass direction left (i.e. North -> West). */
  public void turnLeft() {
    this.direction = this.direction.turnLeft();
    HashMap<String, String> details = this.getSignalDetails();
    details.put(DIRECTION, this.direction.getDirectionString());
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
      throw new NeighborhoodRuntimeException(ExceptionKeys.INVALID_MOVE);
    }
    HashMap<String, String> details = this.getSignalDetails();
    details.put(DIRECTION, this.direction.getDirectionString());
    this.sendOutputMessage(NeighborhoodSignalKey.MOVE, details);
  }

  /**
   * Add paint to the grid at the painter's location.
   *
   * @param color the color of the paint being added
   */
  public void paint(String color) {
    if (this.hasPaint()) {
      this.grid.getSquare(this.xLocation, this.yLocation).setColor(color);
      this.remainingPaint--;
      HashMap<String, String> details = this.getSignalDetails();
      details.put(COLOR, color);
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
    boolean isOnPaint = this.grid.getSquare(this.xLocation, this.yLocation).hasColor();
    this.sendBooleanMessage(NeighborhoodSignalKey.IS_ON_PAINT, isOnPaint);
    return isOnPaint;
  }

  /** @return True if there is a paint bucket in the square where the painter is standing. */
  public boolean isOnBucket() {
    boolean isOnBucket = this.grid.getSquare(this.xLocation, this.yLocation).containsPaint();
    this.sendBooleanMessage(NeighborhoodSignalKey.IS_ON_BUCKET, isOnBucket);
    return isOnBucket;
  }

  /** @return True if the painter's personal bucket has paint in it. */
  public boolean hasPaint() {
    if (this.hasInfinitePaint) {
      return true;
    }
    return this.remainingPaint > 0;
  }

  /** @return True if there is no barrier one square ahead in the requested direction. */
  public boolean canMove(String direction) {
    boolean canMove = this.isValidMovement(Direction.fromString(direction));
    this.sendBooleanMessage(NeighborhoodSignalKey.CAN_MOVE, canMove);
    return canMove;
  }

  /** @return True if there is no barrier one square ahead in the current direction. */
  public boolean canMove() {
    return this.canMove(this.direction.toString());
  }

  /** @return the color of the square where the painter is standing. */
  public String getColor() {
    return this.grid.getSquare(this.xLocation, this.yLocation).getColor();
  }

  /** @return True if facing North */
  public boolean isFacingNorth() {
    return this.direction.isNorth();
  }

  /** @return True if facing East */
  public boolean isFacingEast() {
    return this.direction.isEast();
  }

  /** @return True if facing South */
  public boolean isFacingSouth() {
    return this.direction.isSouth();
  }

  /** @return True if facing West */
  public boolean isFacingWest() {
    return this.direction.isWest();
  }

  /**
   * @deprecated use {@link Painter#isFacingNorth()}
   * @return True if facing North
   */
  public boolean facingNorth() {
    return this.isFacingNorth();
  }

  /**
   * @deprecated use {@link Painter#isFacingEast()}
   * @return True if facing East
   */
  public boolean facingEast() {
    return this.isFacingEast();
  }

  /**
   * @deprecated use {@link Painter#isFacingSouth()}
   * @return True if facing South
   */
  public boolean facingSouth() {
    return this.isFacingSouth();
  }

  /**
   * @deprecated use {@link Painter#isFacingWest()}
   * @return True if facing West
   */
  public boolean facingWest() {
    return this.isFacingWest();
  }

  /** @return the x coordinate of the painter's current position */
  public int getX() {
    return this.xLocation;
  }

  /** @return the y coordinate of the painter's current position */
  public int getY() {
    return this.yLocation;
  }

  /** @return the current direction the painter is facing */
  public String getDirection() {
    return this.direction.getDirectionString();
  }

  public void showBuckets() {
    this.outputAdapter.sendMessage(
        new NeighborhoodSignalMessage(NeighborhoodSignalKey.SHOW_BUCKETS, new HashMap<>()));
  }

  public void hideBuckets() {
    this.outputAdapter.sendMessage(
        new NeighborhoodSignalMessage(NeighborhoodSignalKey.HIDE_BUCKETS, new HashMap<>()));
  }

  /**
   * Sets the amount of paint in the painters bucket. Does nothing if paint is negative.
   *
   * @param paint the amount of paint that should be in the painter's bucket.
   */
  public void setPaint(int paint) {
    if (paint < 0) {
      System.out.println("Paint amount must not be a negative number.");
      return;
    }

    if (this.hasInfinitePaint) {
      return;
    }

    this.remainingPaint = paint;
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
    details.put(ID, this.id);
    return details;
  }

  private void sendOutputMessage(NeighborhoodSignalKey signalKey, HashMap<String, String> details) {
    this.outputAdapter.sendMessage(new NeighborhoodSignalMessage(signalKey, details));
  }

  private void sendBooleanMessage(NeighborhoodSignalKey signalKey, boolean result) {
    HashMap<String, String> details = this.getSignalDetails();
    String resultString = String.valueOf(result);
    details.put(BOOLEAN_RESULT, resultString);
    this.sendOutputMessage(signalKey, details);
  }

  private void sendInitializationMessage() {
    HashMap<String, String> initDetails = this.getSignalDetails();
    initDetails.put(DIRECTION, this.direction.getDirectionString());
    initDetails.put(X, Integer.toString(this.xLocation));
    initDetails.put(Y, Integer.toString(this.yLocation));
    initDetails.put(PAINT, Integer.toString(this.remainingPaint));
    this.sendOutputMessage(NeighborhoodSignalKey.INITIALIZE_PAINTER, initDetails);
  }
}
