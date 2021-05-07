package org.code.neighborhood;

public class Main {
    public static void main(String[] args) {
        // initial setup code from us
//        World w = new World(); // this reads from grid.txt
//        World.setInstance(w);

        // Student code starts here
        Painter art = new Painter(0, 0, "East", 5);
        Grid globalGrid = World.getInstance().getGrid();
        globalGrid.printGrid();
        art.takePaint();
        art.move();
        art.turnLeft();
        art.move();
        art.paint("r");
        art.move();
        art.turnLeft();
        globalGrid.printGrid();
    }
}
