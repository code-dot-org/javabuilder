package org.code.neighborhood;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GridTest {

    @Test
    void constructorBuildsGrid() {
        GridSquare s = new GridSquare(1);
        GridSquare[][] squares = new GridSquare[1][1];
        squares[0][0] = s;
        Grid grid = new Grid(squares);
        assertTrue(grid.validLocation(0, 0));
        assertFalse(grid.validLocation(0, 1));
        assertFalse(grid.validLocation(1, 0));
    }

    @Test
    void validLocationReturnsFalseForInvalidLocations() {
        GridSquare s = new GridSquare(1);
        GridSquare[][] squares = new GridSquare[1][1];
        squares[0][0] = s;
        Grid grid = new Grid(squares);
        assertTrue(grid.validLocation(0, 0));
        assertFalse(grid.validLocation(0, 1));
        assertFalse(grid.validLocation(1, 0));
        assertFalse(grid.validLocation(0, -1));
        assertFalse(grid.validLocation(-1, 0));
    }

    @Test
    void getSquareReturnsSquareForValidLocation() {
        GridSquare s = new GridSquare(1);
        GridSquare[][] squares = new GridSquare[1][1];
        squares[0][0] = s;
        Grid grid = new Grid(squares);
        GridSquare sq = grid.getSquare(0, 0);
        assertEquals(sq, s);
    }

    @Test
    void getSquareThrowsErrorForInvalidLocation() {
        GridSquare s = new GridSquare(1);
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
