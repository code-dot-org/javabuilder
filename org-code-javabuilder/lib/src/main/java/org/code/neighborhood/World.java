package org.code.neighborhood;

public class World {
  private static World worldInstance;
  private Grid grid;

  private World() {
    GridFactory gridFactory = new GridFactory();
    this.grid = gridFactory.createGridFromJSON("grid.txt");
  }

  protected World(String s) {
    GridFactory gridFactory = new GridFactory();
    this.grid = gridFactory.createGridFromString(s);
  }

  protected static void setInstance(World world) {
    worldInstance = world;
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
}
