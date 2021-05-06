package org.code.neighborhood;

import java.util.ArrayList;
import java.io.File;  // Import the File class
import java.io.FileInputStream; // Import the Scanner class to read text files
import java.lang.System;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Grid {
    private static Grid instance = null;
    private GridSquare[][] grid;
    private int width;
    private int height;
    private int numPainters;

    public static Grid getInstance() {
        if (instance == null) {
            File file = new File("grid.txt");
            FileInputStream fis;
            try {
                fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();
//                JSONParser parser = new JSONParser();
                instance = new Grid(new String(data, "UTF-8"));
            } catch (Exception e) {
                System.out.println("\nTry adding a grid.txt file.\n");
                e.printStackTrace();
                System.exit(1);
            }
        }
        return instance;
    }

    private Grid(String gridDescription) {
        String[] lines = gridDescription.split("\n");
        String parseLine = String.join("", lines);
        parseLine.replace("\t", "");
        JSONParser jsonParser = new JSONParser();
        try {
            Object obj = jsonParser.parse(parseLine);
            JSONArray gridSquares = (JSONArray) obj;
            this.height = gridSquares.size();
            if(this.height == 0) {
                System.out.println("\nPlease check the format of your grid.txt file\n");
                System.exit(1);
            }
            this.width = ((JSONArray) gridSquares.get(0)).size();
            this.grid = new GridSquare[this.width][this.height];
            int currentHeight = this.height;
            for (int currentY = 0; currentY < this.height; currentY++) {
                currentHeight--;
                JSONArray line = (JSONArray) gridSquares.get(currentHeight);
                //String[] descriptors = line.split(",");
                if (line.size() != width) {
                    System.out.println("width of line " + line + " does not match others. Cannot create grid.");
                    return;
                }
                for (int currentX = 0; currentX < line.size(); currentX++) {
                    JSONObject descriptor = (JSONObject) line.get(currentX);
                    this.grid[currentX][currentY] = new GridSquare(descriptor);
                }
            }
            this.numPainters = 0;
        } catch(ParseException e) {
            System.out.println("\nPlease check the format of your grid.txt file\n");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public int registerPainter() {
        this.numPainters += 1;
        return this.numPainters;
    }

    public void printGrid() {
        for(int y = height - 1; y >= 0; y--) {
            ArrayList<String> squares = new ArrayList<String>();
            for(int x = 0; x < width; x++) {
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
        if (validLocation(x,y)) {
            return grid[x][y];
        } else {
            throw new UnsupportedOperationException("fail");
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
