package org.code.neighborhood;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;

public class GridFactory {
    protected Grid createGridFromJSON(String filename) {
        File file = new File("grid.txt");
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            return createGridFromString(new String(data, "UTF-8"));
        } catch (Exception e) {
            System.out.println("\nTry adding a grid.txt file.\n");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    protected Grid createGridFromString(String description) {
        String[] lines = description.split("\n");
        String parseLine = String.join("", lines);
        JSONParser jsonParser = new JSONParser();
        try {
            Object obj = jsonParser.parse(parseLine);
            JSONArray gridSquares = (JSONArray) obj;
            int height = gridSquares.size();
            if(height == 0) {
                System.out.println("\nPlease check the format of your grid.txt file\n");
                System.exit(1);
            }
            int width = ((JSONArray) gridSquares.get(0)).size();
            GridSquare[][] grid = new GridSquare[width][height];
            int currentHeight = height;
            for (int currentY = 0; currentY < height; currentY++) {
                currentHeight--;
                JSONArray line = (JSONArray) gridSquares.get(currentHeight);
                if (line.size() != width) {
                    System.out.println("width of line " + line + " does not match others. Cannot create grid.");
                    return null;
                }
                for (int currentX = 0; currentX < line.size(); currentX++) {
                    JSONObject descriptor = (JSONObject) line.get(currentX);
                    grid[currentX][currentY] = new GridSquare(descriptor);
                }
            }
            return new Grid(grid);
        } catch(ParseException e) {
            System.out.println("\nPlease check the format of your grid.txt file\n");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
