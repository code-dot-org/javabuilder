package org.code.neighborhood;

public class Direction {
    private enum CompassDirection{NORTH, EAST, SOUTH, WEST};
    private CompassDirection direction;

    public Direction(String startingDirection) {
        if(startingDirection.equalsIgnoreCase("north")) {
            this.direction = CompassDirection.NORTH;
        } else if(startingDirection.equalsIgnoreCase("east")) {
            this.direction = CompassDirection.EAST;
        } else if(startingDirection.equalsIgnoreCase("south")) {
            this.direction = CompassDirection.SOUTH;
        } else if(startingDirection.equalsIgnoreCase("west")) {
            this.direction = CompassDirection.WEST;
        } else  {
            System.out.println("bad direction");
        }
    }

    //Changes the direction one compassDirection left (i.e. NORTH -> WEST)
    public void turnLeft() {
        if (this.direction == CompassDirection.NORTH) {
            this.direction = CompassDirection.WEST;
        } else {
            this.direction = CompassDirection.values() [this.direction.ordinal() - 1];
        }
        System.out.println("pointing " + this.direction);
    }

    //Returns true if the current direction is north
    public boolean isNorth() {
        return this.direction == CompassDirection.NORTH;
    }

    //Returns true if the current direction is south
    public boolean isSouth() {
        return this.direction == CompassDirection.SOUTH;
    }

    // Returns true if the current direction is east
    public boolean isEast() {
        return this.direction == CompassDirection.EAST;
    }

    // Returns true if the current direction is west
    public boolean isWest() {
        return this.direction == CompassDirection.WEST;
    }
}
