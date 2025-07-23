package org.code.neighborhood.support;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class GridFactoryTest {
  String sampleGrid =
      "[[\n{\"tileType\": 1, \"assetId\": 0}, {\"tileType\": 1, \"assetId\": 0}], \n[{\"tileType\": 1, \"assetId\": 0}, {\"tileType\": 1, \"value\": 4, \"assetId\": 0}]]";

  @Test
  void createGridFromString() {
    GridFactory gridFactory = new GridFactory();
    Grid grid = null;
    try {
      grid = gridFactory.createGridFromString(sampleGrid);
    } catch (IOException e) {
      e.printStackTrace();
    }
    assertTrue(grid instanceof Grid);
    assertTrue(grid.validLocation(1, 1));
  }

  @Test
  void createEmptyGrid() {
    GridFactory gridFactory = new GridFactory();
    Grid grid = gridFactory.createEmptyGrid(2);
    assertTrue(grid instanceof Grid);
    assertTrue(grid.validLocation(1, 1));
  }

  @Test
  void createGridFromStringWithInvalidJSONThrowsException() {
    GridFactory gridFactory = new GridFactory();
    Exception exception =
        assertThrows(
            NeighborhoodRuntimeException.class,
            () -> {
              gridFactory.createGridFromString("not valid json here:");
            });
    String expectedMessage = ExceptionKeys.INVALID_GRID.toString();
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void createGridFromStringWithInvalidGridShapeThrowsException() {
    GridFactory gridFactory = new GridFactory();
    Exception exception =
        assertThrows(
            NeighborhoodRuntimeException.class,
            () -> {
              gridFactory.createGridFromString(
                  "[[\n{\"tileType\": 1, \"assetId\": 0}, {\"tileType\": 1, \"assetId\": 0}], \n[{\"tileType\": 1, \"assetId\": 0}]]");
            });
    String expectedMessage = ExceptionKeys.INVALID_GRID.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }

  @Test
  void createGridFromNotSquareGridThrowsException() {
    GridFactory gridFactory = new GridFactory();
    Exception exception =
        assertThrows(
            NeighborhoodRuntimeException.class,
            () -> {
              gridFactory.createGridFromString(
                  "[[\n{\"tileType\": 1, \"assetId\": 0}], \n[{\"tileType\": 1, \"assetId\": 0}]]");
            });
    String expectedMessage = ExceptionKeys.INVALID_GRID.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }

  @Test
  void createGridFromStringWithInvalidTileTypeThrowsException() {
    GridFactory gridFactory = new GridFactory();
    Exception exception =
        assertThrows(
            NeighborhoodRuntimeException.class,
            () -> {
              gridFactory.createGridFromString("[[\n{\"tileType\": \"invalid\", \"assetId\": 0}]]");
            });
    String expectedMessage = ExceptionKeys.INVALID_GRID.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }

  @Test
  void createGridFromStringWithInvalidAssetIdThrowsException() {
    GridFactory gridFactory = new GridFactory();
    Exception exception =
        assertThrows(
            NeighborhoodRuntimeException.class,
            () -> {
              gridFactory.createGridFromString("[[\n{\"assetId\": \"invalid\", \"tileType\": 1}]]");
            });
    String expectedMessage = ExceptionKeys.INVALID_GRID.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }

  @Test
  void createGridFromStringWithInvalidValueThrowsException() {
    GridFactory gridFactory = new GridFactory();
    Exception exception =
        assertThrows(
            NeighborhoodRuntimeException.class,
            () -> {
              gridFactory.createGridFromString(
                  "[[\n{\"tileType\": 1, \"value\": \"invalid\", \"assetId\": 0}]]");
            });
    String expectedMessage = ExceptionKeys.INVALID_GRID.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }

  @Test
  void creatingEmptyGridThrowsException() {
    GridFactory gridFactory = new GridFactory();
    Exception exception =
        assertThrows(
            NeighborhoodRuntimeException.class,
            () -> {
              gridFactory.createGridFromString("[]");
            });
    String expectedMessage = ExceptionKeys.INVALID_GRID.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }
}
