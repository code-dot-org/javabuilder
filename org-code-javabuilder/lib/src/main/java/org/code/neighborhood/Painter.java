package org.code.neighborhood;

public class Painter {
    private static int lastId = 0;
    private int xLocation;
    private int yLocation;
    private Direction direction;
    private int remainingPaint;
    private Grid grid;
    private String id;

    public Painter(int x, int y, String direction, int paint) {
        this.xLocation = x;
        this.yLocation = y;
        this.direction = Direction.fromString(direction);
        this.remainingPaint = paint;
        this.grid = World.getInstance().getGrid();
        this.id = "Painter-" + lastId++;
        System.out.println("created painter");
    }

    // Turn the painter one compass direction left (i.e. North -> West)
    public void turnLeft() {
        this.direction = this.direction.turnLeft();
    }

    // Move the painter one square forward in the direction the painter
    // is facing
    public void move() {
        if (this.validMovement(this.direction)) {
            if (this.direction.isNorth()) {
                this.yLocation++;
            } else if (this.direction.isSouth()) {
                this.yLocation--;
            } else if (this.direction.isEast()) {
                this.xLocation++;
            } else {
                this.xLocation--;
            }
        } else {
            System.out.println("You can't go that way");
        }
        System.out.println("New (x,y) : (" + this.xLocation + "," + this.yLocation + ")");
    }

    // Add paint of the given color to the grid at the location where the
    // painter currently is
    public void paint(String color) {
        this.grid.getSquare(this.xLocation, this.yLocation).setColor(color);
    }

    // removes all paint on the square where the painter is standing
    public void scrapePaint() {
        this.grid.getSquare(this.xLocation, this.yLocation).removePaint();
    }

    // Returns how many units of paint are in the painter's personal
    // bucket
    public int getMyPaint() {
        return this.remainingPaint;
    }

    // Hides the painter on the screen
    public void hidePainter() {
        System.out.println("You hid the painter");
    }

    // Shows the painter on the screen
    public void showPainter() {
        System.out.println("You displayed the painter");
    }

    // Painter adds a single unit of paint to their personal bucket
    // Counter on the bucket on the screen goes down. If the painter
    // is not standing on a paint bucket, nothing happens
    public void takePaint() {
        if (this.grid.getSquare(this.xLocation, this.yLocation).containsPaint()) {
            this.grid.getSquare(this.xLocation, this.yLocation).collectPaint();
            this.remainingPaint++;
        } else {
            System.out.println("There is no paint to collect here");
        }
    }

    // Returns True if there is paint in the square where the painter
    // is standing.
    public boolean isOnPaint() {
        return this.grid.getSquare(this.xLocation, this.yLocation).hasColor();
    }

    // Returns True if there is a paint bucket in the square where the
    // painter is standing.
    public boolean isOnBucket() {
        return this.grid.getSquare(this.xLocation, this.yLocation).containsPaint();
    }

    // Returns True if remainingPaint > 0
    public boolean hasPaint() {
        return this.remainingPaint > 0;
    }

    private boolean validMovement(Direction direction) {
        if (direction.isNorth()) {
            return this.grid.validLocation(this.xLocation, this.yLocation + 1);
        } else if (this.direction.isSouth()) {
            return this.grid.validLocation(this.xLocation, this.yLocation -1);
        } else if (this.direction.isEast()) {
            return this.grid.validLocation(this.xLocation + 1, this.yLocation);
        } else {
            return this.grid.validLocation(this.xLocation - 1, this.yLocation);
        }
    }

    // Returns True if there is no barrier one square ahead in the
    // requested direction.
    public boolean canMove(String direction) {
        return validMovement(Direction.fromString(direction));
    }

    // Returns the color of the square where the painter is standing
    public String getColor() {
        return this.grid.getSquare(this.xLocation, this.yLocation).getColor();
    }

    // returns True if facing North.
    public boolean facingNorth() {
        return this.direction.isNorth();
    }

    // returns True if facing East.
    public boolean facingEast() {
        return this.direction.isEast();
    }

    // returns True if facing South.
    public boolean facingSouth() {
        return this.direction.isSouth();
    }

    // returns True if facing West.
    public boolean facingWest() {
        return this.direction.isWest();
    }
}
