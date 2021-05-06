package org.code.neighborhood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class GridTest {
    String sampleGrid = "[[{\"tileType\": 1}, {\"tileType\": 1}], [{\"tileType\": 1}, {\"tileType\": 1}]]";

    @Test
    void constructorBuildsGrid() {
        Grid grid = Grid.getInstance();
        assertEquals(grid.validLocation(0, 0), true);
        assertEquals(grid.validLocation(0, 1), true);
        assertEquals(grid.validLocation(1, 0), true);
        assertEquals(grid.validLocation(1, 1), true);
        assertEquals(grid.validLocation(0, 2), false);
        assertEquals(grid.validLocation(2, 0), false);
    }
    // check construction of grid
    // check register painter
    /// print grid
    // validLocation
    // getSquare
}
