package org.code.neighborhood.support;

public enum Direction {
  NORTH("north"),
  EAST("east"),
  SOUTH("south"),
  WEST("west");
  private String directionString;

  Direction(String directionString) {
    this.directionString = directionString;
  }

  public Direction turnLeft() {
    Direction newDir;
    if (this == NORTH) {
      newDir = WEST;
    } else {
      newDir = this.values()[this.ordinal() - 1];
    }
    return newDir;
  }

  public String getDirectionString() {
    return directionString;
  }

  // Returns true if the current direction is north
  public boolean isNorth() {
    return this == NORTH;
  }

  // Returns true if the current direction is south
  public boolean isSouth() {
    return this == SOUTH;
  }

  // Returns true if the current direction is east
  public boolean isEast() {
    return this == EAST;
  }

  // Returns true if the current direction is west
  public boolean isWest() {
    return this == WEST;
  }

  public static Direction fromString(String text) {
    if (text.equalsIgnoreCase("north")) {
      return NORTH;
    } else if (text.equalsIgnoreCase("east")) {
      return EAST;
    } else if (text.equalsIgnoreCase("south")) {
      return SOUTH;
    } else if (text.equalsIgnoreCase("west")) {
      return WEST;
    } else {
      throw new NeighborhoodRuntimeException(ExceptionKeys.INVALID_DIRECTION);
    }
  }
}
