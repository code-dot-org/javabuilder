package org.code.neighborhood.support;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class DirectionTest {
  @Test
  void constructorIgnoresCase() {
    Direction dir = Direction.fromString("NoRtH");
    assertEquals(dir, Direction.NORTH);
  }

  @Test
  void throwsErrorIfBadDirectionGiven() {
    Exception exception =
        assertThrows(
            NeighborhoodRuntimeException.class,
            () -> {
              Direction.fromString("not a direction");
            });
    String expectedMessage = ExceptionKeys.INVALID_DIRECTION.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }

  @Test
  void facesWestAfterTurningLeftFromNorth() {
    Direction dir = Direction.fromString("North");
    dir = dir.turnLeft();
    assertTrue(dir.isWest());
  }

  @Test
  void facesNorthAfterTurningLeftFromEast() {
    Direction dir = Direction.fromString("East");
    dir = dir.turnLeft();
    assertTrue(dir.isNorth());
  }
}
