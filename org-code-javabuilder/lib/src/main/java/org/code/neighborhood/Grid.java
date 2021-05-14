package org.code.neighborhood;

import java.util.ArrayList;

public class Grid {
  private GridSquare[][] grid;
  private int width;
  private int height;

  protected Grid(GridSquare[][] squares) {
    this.grid = squares;
    this.height = squares.length;
    this.width = squares[0].length;
  }

  public void printGrid() {
    for (int y = height - 1; y >= 0; y--) {
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
      throw new UnsupportedOperationException(ExceptionKeys.GET_SQUARE_FAILED.toString());
    }
  }

  // Hides all buckets from the screen
  public void hideBuckets() {
    System.out.println("You hid the buckets");
  }

  // Displays all buckets on the screen
  public void showBuckets() {
    System.out.println("You displayed the buckets");
  }

  protected int getSize() {
    return this.grid.length;
  }
}
