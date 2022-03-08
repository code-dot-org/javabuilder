package org.code.neighborhood;

import java.util.ArrayList;
import java.util.HashMap;
import org.code.protocol.OutputAdapter;

public class Grid {
  private final GridSquare[][] grid;
  private final int width;
  private final int height;
  private final OutputAdapter outputAdapter;

  protected Grid(GridSquare[][] squares, OutputAdapter outputAdapter) {
    this.grid = squares;
    this.height = squares.length;
    this.width = squares[0].length;
    this.outputAdapter = outputAdapter;
  }

  public void printGrid() {
    for (int y = 0; y < height; y++) {
      ArrayList<String> squares = new ArrayList<String>();
      for (int x = 0; x < width; x++) {
        squares.add(this.grid[x][y].getPrintableDescription());
      }
      System.out.println(String.join(",", squares));
    }
  }

  // Determines whether the given coordinate can be moved into
  // A coordinate cannot be moved into if it is out of the range of the grid
  // or if the tile is not passable (wall, obstacle, or unknown tile)
  public boolean validLocation(int x, int y) {
    return x >= 0 && y >= 0 && x < width && y < height && this.grid[x][y].isPassable();
  }

  // Returns the GridSquare at the given position
  public GridSquare getSquare(int x, int y) {
    if (validLocation(x, y)) {
      return this.grid[x][y];
    } else {
      throw new NeighborhoodRuntimeException(ExceptionKeys.GET_SQUARE_FAILED);
    }
  }

  /** Hides all buckets from the screen */
  public void hideBuckets() {
    this.outputAdapter.sendMessage(
        new NeighborhoodSignalMessage(NeighborhoodSignalKey.HIDE_BUCKETS, new HashMap<>()));
  }

  /** Displays all buckets on the screen */
  public void showBuckets() {
    this.outputAdapter.sendMessage(
        new NeighborhoodSignalMessage(NeighborhoodSignalKey.SHOW_BUCKETS, new HashMap<>()));
  }

  public int getSize() {
    return this.grid.length;
  }
}
