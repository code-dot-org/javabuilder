package org.code.neighborhood;

import java.io.File;
import java.io.FileInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GridFactory {
  private static final String GRID_FILE_NAME = "grid.txt";
  private static final String GRID_SQUARE_TYPE_FIELD = "tileType";
  private static final String GRID_SQUARE_VALUE_FIELD = "value";

  protected Grid createGridFromJSON(String filename) {
    File file = new File(GRID_FILE_NAME);
    FileInputStream fis;
    try {
      fis = new FileInputStream(file);
      byte[] data = new byte[(int) file.length()];
      fis.read(data);
      fis.close();
      return createGridFromString(new String(data, "UTF-8"));
    } catch (Exception e) {
      throw new UnsupportedOperationException("Try adding a grid.txt file.");
    }
  }

  // Creates a grid from a string, assuming that the string is a 2D JSONArray of JSONObjects
  // with each JSONObject containing an integer tileType and optionally an integer value
  // corresponding with the paintCount for that tile.
  protected Grid createGridFromString(String description) {
    String[] lines = description.split("\n");
    String parseLine = String.join("", lines);
    JSONParser jsonParser = new JSONParser();
    try {
      Object obj = jsonParser.parse(parseLine);
      JSONArray gridSquares = (JSONArray) obj;
      int height = gridSquares.size();
      if (height == 0) {
        throw new UnsupportedOperationException("Please check the format of your grid.txt file");
      }
      int width = ((JSONArray) gridSquares.get(0)).size();
      GridSquare[][] grid = new GridSquare[width][height];

      // We start at the maximum height because we're reading the grid from top to bottom in the
      // file.
      int currentHeight = height;
      for (int currentY = 0; currentY < height; currentY++) {
        currentHeight--;
        JSONArray line = (JSONArray) gridSquares.get(currentHeight);
        if (line.size() != width) {
          throw new UnsupportedOperationException(
              "width of line " + currentHeight + " does not match others. Cannot create grid.");
        }
        for (int currentX = 0; currentX < line.size(); currentX++) {
          JSONObject descriptor = (JSONObject) line.get(currentX);
          try {
            int tileType = Integer.parseInt(descriptor.get(GRID_SQUARE_TYPE_FIELD).toString());
            if (descriptor.containsKey(GRID_SQUARE_VALUE_FIELD)) {
              int value = Integer.parseInt(descriptor.get(GRID_SQUARE_VALUE_FIELD).toString());
              grid[currentX][currentY] = new GridSquare(tileType, value);
            } else {
              grid[currentX][currentY] = new GridSquare(tileType);
            }
          } catch (NumberFormatException e) {
            throw new UnsupportedOperationException(
                "Please check the format of your grid.txt file");
          }
        }
      }
      return new Grid(grid);
    } catch (ParseException e) {
      throw new UnsupportedOperationException("Please check the format of your grid.txt file");
    }
  }
}
