package org.code.neighborhood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class DirectionTest {
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void facesWestAfterTurningLeftFromNorth() {
        Direction dir = new Direction("North");
        dir.turnLeft();
        assertEquals(dir.isWest(), true);
        assertTrue(outputStreamCaptor.toString().trim().contains("pointing WEST"));
    }

    @Test
    void facesNorthAfterTurningLeftFromEast() {
        Direction dir = new Direction("East");
        dir.turnLeft();
        assertEquals(dir.isNorth(), true);
        assertTrue(outputStreamCaptor.toString().trim().contains("pointing NORTH"));
    }

    @Test
    void printsErrorMessageIfBadDirectionGiven() {
        Direction dir = new Direction("not a direction");
        assertTrue(outputStreamCaptor.toString().trim().contains("bad direction"));
    }
}
