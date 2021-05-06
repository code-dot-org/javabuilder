package org.code.neighborhood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class DirectionTest {
    @Test
    void facesWestAfterTurningLeftFromNorth() {
        Direction dir = new Direction("North");
        dir.turnLeft();
        assertEquals(dir.isWest(), true);
    }

    @Test
    void facesNorthAfterTurningLeftFromEast() {
        Direction dir = new Direction("East");
        dir.turnLeft();
        assertEquals(dir.isNorth(), true);
        System.out.println(System.currentTimeMillis());
    }
}
