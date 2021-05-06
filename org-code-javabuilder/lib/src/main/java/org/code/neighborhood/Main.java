package org.code.neighborhood;

public class Main {
    public static void main(String[] args) {
        Painter art = new Painter(0, 0, "East", 5);
        Grid globalGrid = Grid.getInstance();
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
