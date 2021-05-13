package org.code.neighborhood;

import java.io.IOException;

public class World {
  private static World worldInstance;
  private Grid grid;

  public World(String s) {
    GridFactory gridFactory = new GridFactory();
    try {
      this.grid = gridFactory.createGridFromString(s);
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Something went wrong with reading the grid.");
      System.exit(0);
    }
  }

  private World() {
    GridFactory gridFactory = new GridFactory();
    try {
      this.grid = gridFactory.createGridFromJSON("grid.txt");
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Something went wrong with reading the grid.");
      System.exit(0);
    }
  }

  public static World getInstance() {
    if (worldInstance == null) {
      worldInstance = new World();
    }
    return worldInstance;
  }

  public Grid getGrid() {
    return this.grid;
  }

  protected static void setInstance(World world) {
    worldInstance = world;
  }
}
