package org.code.neighborhood;

public class GridSquare {
    private String color;
    private boolean passable;
    private int paintCount;
    private enum SquareType{ WALL, OPEN, START, FINISH, OBSTACLE, STARTANDFINISH, UNKNOWN};
    private SquareType squareType;

    protected GridSquare(int tileType, int value) {
        this.setTileType(tileType);
        this.paintCount = value;
        this.color = "";
    }

    protected GridSquare(int tileType) {
        this.setTileType(tileType);
        this.paintCount = 0;
        this.color = "";
    }

    private void setTileType(int tileType) {
        switch(tileType) {
            case 0:
                this.squareType = SquareType.WALL;
                this.passable = false;
                break;
            case 1:
                this.squareType = SquareType.OPEN;
                this.passable = true;
                break;
            case 2:
                this.squareType = SquareType.START;
                this.passable = true;
                break;
            case 3:
                this.squareType = SquareType.FINISH;
                this.passable = true;
                break;
            case 4:
                this.squareType = SquareType.OBSTACLE;
                this.passable = false;
                break;
            case 5:
                this.squareType = SquareType.STARTANDFINISH;
                this.passable = true;
                break;
            default:
                this.squareType = SquareType.UNKNOWN;
                this.passable = false;
        }
    }

    // Sets the color of the square to the given color
    public void setColor(String color) {
        if (!Helpers.isColor(color)) {
            System.out.println("Invalid color, please check your color format");
            return;
        }
        if (this.passable && this.paintCount == 0) {
            this.color = color;
        }
    }

    // Determines whether the given coordinate can be moved into
    public boolean isPassable() {
        return this.passable;
    }

    // Decreases the paintCount by 1 if there is available paint
    public void collectPaint() {
        if (this.containsPaint()) {
            this.paintCount--;
        } else {
            System.out.println("There's no paint to collect here");
        }
    }

    // Returns the square to a non-painted state
    public void removePaint() {
        if (!this.color.equals("")) {
            this.color = "";
        } else {
            System.out.println("There's no paint to remove here");
        }
    }

    // Returns true if the square has paint available to collect
    public boolean containsPaint() {
        return this.paintCount > 0;
    }

    public String getPrintableDescription() {
        if (!this.passable) {
            return "x";
        } else if (!this.color.equals("")) {
            return this.color;
        } else {
            return String.valueOf(this.paintCount);
        }
    }

    // Returns true if the color variable is populated
    public boolean hasColor() {
        return !this.color.equals("");
    }

    // Returns the color of the square
    public String getColor() {
        return this.color;
    }
}
