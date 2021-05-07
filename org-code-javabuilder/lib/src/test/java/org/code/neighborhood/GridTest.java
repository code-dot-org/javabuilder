package org.code.neighborhood;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GridTest {

    @Test
    void constructorBuildsGrid() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        GridSquare s = new GridSquare(description);
        GridSquare[][] squares = new GridSquare[1][1];
        squares[0][0] = s;
        Grid grid = new Grid(squares);
        assertEquals(grid.validLocation(0, 0), true);
        assertEquals(grid.validLocation(0, 1), false);
        assertEquals(grid.validLocation(1, 0), false);
    }

    @Test
    void validLocationReturnsFalseForInvalidLocations() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        GridSquare s = new GridSquare(description);
        GridSquare[][] squares = new GridSquare[1][1];
        squares[0][0] = s;
        Grid grid = new Grid(squares);
        assertEquals(grid.validLocation(0, 0), true);
        assertEquals(grid.validLocation(0, 1), false);
        assertEquals(grid.validLocation(1, 0), false);
        assertEquals(grid.validLocation(0, -1), false);
        assertEquals(grid.validLocation(-1, 0), false);
    }

    @Test
    void getSquareReturnsSquareForValidLocation() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        GridSquare s = new GridSquare(description);
        GridSquare[][] squares = new GridSquare[1][1];
        squares[0][0] = s;
        Grid grid = new Grid(squares);
        GridSquare sq = grid.getSquare(0, 0);
        assertEquals(sq, s);
    }

    @Test
    void getSquareThrowsErrorForInvalidLocation() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        GridSquare s = new GridSquare(description);
        GridSquare[][] squares = new GridSquare[1][1];
        squares[0][0] = s;
        Grid grid = new Grid(squares);
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            GridSquare sq = grid.getSquare(-1, -1);
        });

        String expectedMessage = "failed to get square";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}
