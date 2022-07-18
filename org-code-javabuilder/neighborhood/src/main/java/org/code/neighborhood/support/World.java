package org.code.neighborhood.support;

import java.io.IOException;
import org.code.protocol.*;

public class World extends JavabuilderSharedObject {
  private final Grid grid;

  public World(int size) {
    GridFactory gridFactory = new GridFactory();
    this.grid = gridFactory.createEmptyGrid(size);
  }

  public World(String s) {
    GridFactory gridFactory = new GridFactory();
    try {
      this.grid = gridFactory.createGridFromString(s);
    } catch (IOException e) {
      throw new InternalServerRuntimeException(InternalExceptionKey.INTERNAL_EXCEPTION, e);
    }
  }

  public World() {
    GridFactory gridFactory = new GridFactory();
    try {
      this.grid = gridFactory.createGridFromJSON("grid.txt");
    } catch (IOException e) {
      throw new InternalServerRuntimeException(InternalExceptionKey.INTERNAL_EXCEPTION, e);
    }
  }

  public Grid getGrid() {
    return this.grid;
  }
}
