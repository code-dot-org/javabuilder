package org.code.neighborhood;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.json.simple.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class GridSquareTest {
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
    void wallsNotPassable() {
        GridSquare s = new GridSquare(0);
        assertEquals(s.isPassable(), false);
    }

    @Test
    void obstaclesNotPassable() {
        GridSquare s = new GridSquare(4);
        assertEquals(s.isPassable(), false);
    }

    @Test
    void unknownTileTypeNotPassable() {
        GridSquare s = new GridSquare(-1);
        assertEquals(s.isPassable(), false);
    }

    @Test
    void openTileTypePassable() {
        GridSquare s = new GridSquare(1);
        assertEquals(s.isPassable(), true);
    }

    @Test
    void startTileTypePassable() {
        GridSquare s = new GridSquare(2);
        assertEquals(s.isPassable(), true);
    }

    @Test
    void finishTileTypePassable() {
        GridSquare s = new GridSquare(3);
        assertEquals(s.isPassable(), true);
    }

    @Test
    void startAndFinishTileTypePassable() {
        GridSquare s = new GridSquare(5);
        assertEquals(s.isPassable(), true);
    }

    @Test
    void constructorWithValueProvidedSetsPaintCount() {
        GridSquare s = new GridSquare(1, 4);
        assertEquals(s.containsPaint(), true);
    }

    @Test
    void defaultPaintCountIsNoPaint() {
        GridSquare s = new GridSquare(1);
        assertEquals(s.containsPaint(), false);
    }

    @Test
    void setColorChecksColorFormatBeforeSettingColor() {
        GridSquare s = new GridSquare(1);
        s.setColor("red");
        assertEquals(s.getColor(), "");
        assertTrue(outputStreamCaptor.toString().trim().contains("use a 1 letter character to set your color"));
        s.setColor("r");
        assertEquals(s.getColor(), "r");
        s.setColor("green");
        assertEquals(s.getColor(), "r");
    }

    @Test
    void setColorDoesNotChangeColorIfThereIsPaint() {
        GridSquare s = new GridSquare(1, 4);
        s.setColor("r");
        assertEquals(s.getColor(), "");
    }

    @Test
    void removePaint() {
        GridSquare s = new GridSquare(1);
        s.setColor("r");
        assertEquals(s.getColor(), "r");
        s.removePaint();
        assertEquals(s.getColor(), "");
    }

    @Test
    void removePaintPrintsErrorWhenNoPaint() {
        GridSquare s = new GridSquare(1);
        assertEquals(s.getColor(), "");
        s.removePaint();
        assertTrue(outputStreamCaptor.toString().trim().contains("There's no paint to remove here"));
    }

    @Test
    void containsPaint() {
        GridSquare noPaint = new GridSquare(1);
        assertEquals(noPaint.containsPaint(), false);

        GridSquare withPaint = new GridSquare(1, 2);
        assertEquals(withPaint.containsPaint(), true);
    }


    @Test
    void collectPaint() {
        GridSquare s = new GridSquare(1, 2);
        assertEquals(s.containsPaint(), true);
        s.collectPaint();
        // paintCount should be 1
        assertEquals(s.containsPaint(), true);
        s.collectPaint();
        // paintCount should be 0
        assertEquals(s.containsPaint(), false);
        s.collectPaint();
        assertTrue(outputStreamCaptor.toString().trim().contains("There's no paint to collect here"));
        // paintCount should be 0
        assertEquals(s.containsPaint(), false);
    }

    @Test
    void getPrintableDescriptionReturnsXForNotPassable() {
        GridSquare s = new GridSquare(0);
        assertEquals(s.isPassable(), false);
        assertEquals(s.getPrintableDescription(), "x");
    }

    @Test
    void getPrintableDescriptionReturnsColorForPassableWithColor() {
        GridSquare s = new GridSquare(1);
        assertEquals(s.isPassable(), true);
        s.setColor("r");
        assertEquals(s.getPrintableDescription(), "r");
    }

    @Test
    void getPrintableDescriptionReturnsPaintCountForPassableWithoutColor() {
        GridSquare s = new GridSquare(1, 4);
        assertEquals(s.isPassable(), true);
        assertEquals(s.getPrintableDescription(), "4");
    }

    @Test
    void hasColor() {
        GridSquare s = new GridSquare(1);
        assertEquals(s.hasColor(), false);
        s.setColor("r");
        assertEquals(s.hasColor(), true);
    }
}
