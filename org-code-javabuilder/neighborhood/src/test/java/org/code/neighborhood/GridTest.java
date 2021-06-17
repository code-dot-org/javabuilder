package org.code.neighborhood;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.code.protocol.OutputAdapter;
import org.junit.jupiter.api.Test;

public class GridTest {

  @Test
  void constructorBuildsGrid() {
    GridSquare s = new GridSquare(1, 0);
    GridSquare[][] squares = new GridSquare[1][1];
    squares[0][0] = s;
    Grid grid = new Grid(squares, mock(OutputAdapter.class));
  }

  @Test
  void validLocationReturnsFalseForInvalidLocations() {
    GridSquare s = new GridSquare(1, 0);
    GridSquare[][] squares = new GridSquare[1][1];
    squares[0][0] = s;
    Grid grid = new Grid(squares, mock(OutputAdapter.class));
    assertTrue(grid.validLocation(0, 0));
    assertFalse(grid.validLocation(0, 1));
    assertFalse(grid.validLocation(1, 0));
    assertFalse(grid.validLocation(0, -1));
    assertFalse(grid.validLocation(-1, 0));
  }

  @Test
  void getSquareReturnsSquareForValidLocation() {
    GridSquare s = new GridSquare(1, 0);
    GridSquare[][] squares = new GridSquare[1][1];
    squares[0][0] = s;
    Grid grid = new Grid(squares, mock(OutputAdapter.class));
    GridSquare sq = grid.getSquare(0, 0);
    assertEquals(sq, s);
  }

  @Test
  void getSquareThrowsErrorForInvalidLocation() {
    GridSquare s = new GridSquare(1, 0);
    GridSquare[][] squares = new GridSquare[1][1];
    squares[0][0] = s;
    Grid grid = new Grid(squares, mock(OutputAdapter.class));
    Exception exception =
        assertThrows(
            NeighborhoodRuntimeException.class,
            () -> {
              GridSquare sq = grid.getSquare(-1, -1);
            });

    String expectedMessage = ExceptionKeys.GET_SQUARE_FAILED.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }
}
