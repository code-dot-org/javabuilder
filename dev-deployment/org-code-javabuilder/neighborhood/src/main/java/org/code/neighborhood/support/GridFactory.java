package org.code.neighborhood.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GridFactory {
  private static final String GRID_FILE_NAME = "grid.txt";
  private static final String GRID_SQUARE_TYPE_FIELD = "tileType";
  private static final String GRID_SQUARE_ASSET_ID_FIELD = "assetId";
  private static final String GRID_SQUARE_VALUE_FIELD = "value";

  protected GridFactory() {}

  protected Grid createGridFromJSON(String filename) throws IOException {
    File file = new File(GRID_FILE_NAME);
    FileInputStream fis;
    try {
      fis = new FileInputStream(file);
      byte[] data = new byte[(int) file.length()];
      fis.read(data);
      fis.close();
      return createGridFromString(new String(data, "UTF-8"));
    } catch (IOException e) {
      throw new NeighborhoodRuntimeException(ExceptionKeys.INVALID_GRID);
    }
  }

  // Creates a grid from a string, assuming that the string is a 2D JSONArray of JSONObjects
  // with each JSONObject containing an integer tileType and optionally an integer value
  // corresponding with the paintCount for that tile.
  protected Grid createGridFromString(String description) throws IOException {
    String[] lines = description.split("\n");
    String parseLine = String.join("", lines);
    try {
      JSONArray gridSquares = new JSONArray(parseLine);
      int height = gridSquares.length();
      if (height == 0) {
        throw new NeighborhoodRuntimeException(ExceptionKeys.INVALID_GRID);
      }
      int width = ((JSONArray) gridSquares.get(0)).length();
      if (width != height) {
        throw new NeighborhoodRuntimeException(ExceptionKeys.INVALID_GRID);
      }
      GridSquare[][] grid = new GridSquare[width][height];

      // Read the grid from top to bottom
      for (int currentY = 0; currentY < height; currentY++) {
        JSONArray line = (JSONArray) gridSquares.get(currentY);
        if (line.length() != width) {
          throw new NeighborhoodRuntimeException(ExceptionKeys.INVALID_GRID);
        }
        for (int currentX = 0; currentX < line.length(); currentX++) {
          JSONObject descriptor = (JSONObject) line.get(currentX);
          try {
            int tileType = Integer.parseInt(descriptor.get(GRID_SQUARE_TYPE_FIELD).toString());
            int assetId = 0;
            if (!descriptor.isNull(GRID_SQUARE_ASSET_ID_FIELD)) {
              assetId = Integer.parseInt(descriptor.get(GRID_SQUARE_ASSET_ID_FIELD).toString());
            }
            if (descriptor.has(GRID_SQUARE_VALUE_FIELD)) {
              int value = Integer.parseInt(descriptor.get(GRID_SQUARE_VALUE_FIELD).toString());
              grid[currentX][currentY] = new GridSquare(tileType, assetId, value);
            } else {
              grid[currentX][currentY] = new GridSquare(tileType, assetId);
            }
          } catch (NumberFormatException e) {
            throw new NeighborhoodRuntimeException(ExceptionKeys.INVALID_GRID);
          }
        }
      }
      return new Grid(grid);
    } catch (JSONException e) {
      throw new NeighborhoodRuntimeException(ExceptionKeys.INVALID_GRID);
    }
  }

  // Creates an empty size x size grid with every square being open
  // and having assetId 0.
  protected Grid createEmptyGrid(int size) {
    GridSquare[][] grid = new GridSquare[size][size];
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        grid[i][j] = new GridSquare(1, 0);
      }
    }
    return new Grid(grid);
  }
}
