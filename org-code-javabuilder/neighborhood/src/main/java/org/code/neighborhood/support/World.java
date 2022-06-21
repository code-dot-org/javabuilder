package org.code.neighborhood.support;

import java.io.IOException;
import org.code.protocol.*;

public class World {
  private static World worldInstance;
  private final Grid grid;

  private static class CloseListener implements LifecycleListener {
    @Override
    public void onExecutionEnded() {
      World.setInstance(null);
    }
  }

  public World(int size) {
    this.registerLifecycleListener();
    GridFactory gridFactory = new GridFactory();
    this.grid = gridFactory.createEmptyGrid(size);
  }

  public World(String s) {
    this.registerLifecycleListener();
    GridFactory gridFactory = new GridFactory();
    try {
      this.grid = gridFactory.createGridFromString(s);
    } catch (IOException e) {
      throw new InternalServerRuntimeException(InternalExceptionKey.INTERNAL_EXCEPTION, e);
    }
  }

  private World() {
    this.registerLifecycleListener();
    GridFactory gridFactory = new GridFactory();
    try {
      this.grid = gridFactory.createGridFromJSON("grid.txt");
    } catch (IOException e) {
      throw new InternalServerRuntimeException(InternalExceptionKey.INTERNAL_EXCEPTION, e);
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

  public static void setInstance(World world) {
    worldInstance = world;
  }

  private void registerLifecycleListener() {
    GlobalProtocol.getInstance().registerLifecycleListener(new CloseListener());
  }
}
