package org.code.neighborhood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

public class GridTest {
    String sampleGrid = "[[{\"tileType\": 1}, {\"tileType\": 1}], [{\"tileType\": 1}, {\"tileType\": 1}]]";

    @Test
    void constructorBuildsGrid() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        GridSquare s = new GridSquare(description);
        GridSquare[][] squares = new GridSquare[1][1];
        squares[0][0] = s;
        Grid grid = new Grid(squares);
        assertEquals(grid.validLocation(0, 0), true);
    }
    // check construction of grid
    // check register painter
    /// print grid
    // validLocation
    // getSquare
}
