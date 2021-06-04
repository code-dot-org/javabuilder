package org.code.neighborhood;

import org.code.protocol.GlobalProtocol;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.InternalJavabuilderError;
import org.code.protocol.OutputAdapter;

import java.io.IOException;

public class World {
  private static World worldInstance;
  private final Grid grid;
  private final OutputAdapter outputAdapter;

  public World(int size) {
    this.outputAdapter = GlobalProtocol.getInstance().getOutputAdapter();
    GridFactory gridFactory = new GridFactory();
    this.grid = gridFactory.createEmptyGrid(size);
  }

  public World(String s) {
    this.outputAdapter = GlobalProtocol.getInstance().getOutputAdapter();
    GridFactory gridFactory = new GridFactory();
    try {
      this.grid = gridFactory.createGridFromString(s);
    } catch (IOException e) {
      throw new InternalJavabuilderError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }

  private World() {
    this.outputAdapter = GlobalProtocol.getInstance().getOutputAdapter();
    GridFactory gridFactory = new GridFactory();
    try {
      this.grid = gridFactory.createGridFromJSON("grid.txt");
    } catch (IOException e) {
      throw new InternalJavabuilderError(InternalErrorKey.INTERNAL_EXCEPTION, e);
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

  public OutputAdapter getOutputAdapter() {
    return this.outputAdapter;
  }

  public static void setInstance(World world) {
    worldInstance = world;
  }
}
