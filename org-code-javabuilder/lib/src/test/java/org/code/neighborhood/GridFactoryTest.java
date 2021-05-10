package org.code.neighborhood;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GridFactoryTest {
    String sampleGrid = "[[\n{\"tileType\": 1}, {\"tileType\": 1}], \n[{\"tileType\": 1}, {\"tileType\": 1, \"value\": 4}]]";

    @Test
    void createGridFromString() {
        GridFactory gridFactory = new GridFactory();
        Grid grid = gridFactory.createGridFromString(sampleGrid);
        assertTrue(grid instanceof Grid);
        assertTrue(grid.validLocation(1, 1));
    }

    @Test
    void createGridFromStringWithInvalidJSONThrowsException() {
        GridFactory gridFactory = new GridFactory();
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            gridFactory.createGridFromString("not valid json here:");
        });
        String expectedMessage = "Please check the format of your grid.txt file";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void createGridFromStringWithInvalidGridShapeThrowsException() {
        GridFactory gridFactory = new GridFactory();
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            gridFactory.createGridFromString("[[\n{\"tileType\": 1}, {\"tileType\": 1}], \n[{\"tileType\": 1}]]");
        });
        String expectedMessage = "width of line 1 does not match others. Cannot create grid.";
        assertEquals(exception.getMessage(), expectedMessage);
    }

    @Test
    void createGridFromStringWithInvalidTileTypeThrowsException() {
        GridFactory gridFactory = new GridFactory();
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            gridFactory.createGridFromString("[[\n{\"tileType\": \"invalid\"}]]");
        });
        String expectedMessage = "Please check the format of your grid.txt file";
        assertEquals(exception.getMessage(), expectedMessage);
    }

    @Test
    void createGridFromStringWithInvalidValueThrowsException() {
        GridFactory gridFactory = new GridFactory();
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            gridFactory.createGridFromString("[[\n{\"tileType\": 1, \"value\": \"invalid\"}]]");
        });
        String expectedMessage = "Please check the format of your grid.txt file";
        assertEquals(exception.getMessage(), expectedMessage);
    }

    @Test
    void creatingEmptyGridThrowsException() {
        GridFactory gridFactory = new GridFactory();
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            gridFactory.createGridFromString("[]");
        });
        String expectedMessage = "Please check the format of your grid.txt file";
        assertEquals(exception.getMessage(), expectedMessage);
    }
}
