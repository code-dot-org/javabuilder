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
        squares.add(grid[x][y].getPrintableDescription());
      }
      System.out.println(String.join(",", squares));
    }
  }

  // Determines whether the given coordinate can be moved into
  public boolean validLocation(int x, int y) {
    return x >= 0 && y >= 0 && x < width && y < height && grid[x][y].isPassable();
  }

  // Returns the GridSquare at the given position
  public GridSquare getSquare(int x, int y) {
    if (validLocation(x, y)) {
      return grid[x][y];
    } else {
      throw new UnsupportedOperationException("failed to get square");
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
}
