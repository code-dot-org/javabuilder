package org.code.neighborhood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.json.simple.JSONObject;

public class GridSquareTest {

    @Test
    void wallsNotPassable() {
        JSONObject description = new JSONObject();
        description.put("tileType", 0);
        GridSquare s = new GridSquare(description);
        assertEquals(s.isPassable(), false);
    }

    @Test
    void obstaclesNotPassable() {
        JSONObject description = new JSONObject();
        description.put("tileType", 4);
        GridSquare s = new GridSquare(description);
        assertEquals(s.isPassable(), false);
    }

    @Test
    void unknownTileTypeNotPassable() {
        JSONObject description = new JSONObject();
        description.put("tileType", -1);
        GridSquare s = new GridSquare(description);
        assertEquals(s.isPassable(), false);
    }

    @Test
    void openTileTypePassable() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        GridSquare s = new GridSquare(description);
        assertEquals(s.isPassable(), true);
    }

    @Test
    void startTileTypePassable() {
        JSONObject description = new JSONObject();
        description.put("tileType", 2);
        GridSquare s = new GridSquare(description);
        assertEquals(s.isPassable(), true);
    }

    @Test
    void finishTileTypePassable() {
        JSONObject description = new JSONObject();
        description.put("tileType", 3);
        GridSquare s = new GridSquare(description);
        assertEquals(s.isPassable(), true);
    }

    @Test
    void startAndFinishTileTypePassable() {
        JSONObject description = new JSONObject();
        description.put("tileType", 5);
        GridSquare s = new GridSquare(description);
        assertEquals(s.isPassable(), true);
    }

    @Test
    void constructorWithValueProvidedSetsPaintCount() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        description.put("value", 4);
        GridSquare s = new GridSquare(description);
        assertEquals(s.containsPaint(), true);
    }

    @Test
    void defaultPaintCountIsNoPaint() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        GridSquare s = new GridSquare(description);
        assertEquals(s.containsPaint(), false);
    }

    @Test
    void setColorChecksColorFormatBeforeSettingColor() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        GridSquare s = new GridSquare(description);
        s.setColor("red");
        assertEquals(s.getColor(), "");
        s.setColor("r");
        assertEquals(s.getColor(), "r");
        s.setColor("green");
        assertEquals(s.getColor(), "r");
    }

    @Test
    void setColorDoesNotChangeColorIfThereIsPaint() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        description.put("value", 4);
        GridSquare s = new GridSquare(description);
        s.setColor("r");
        assertEquals(s.getColor(), "");
    }

    @Test
    void removePaint() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        GridSquare s = new GridSquare(description);
        s.setColor("r");
        assertEquals(s.getColor(), "r");
        s.removePaint();
        assertEquals(s.getColor(), "");
    }

    @Test
    void containsPaint() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        GridSquare noPaint = new GridSquare(description);
        assertEquals(noPaint.containsPaint(), false);
        description.put("value", 2);
        GridSquare withPaint = new GridSquare(description);
        assertEquals(withPaint.containsPaint(), true);
    }


    @Test
    void collectPaint() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        description.put("value", 2);
        GridSquare s = new GridSquare(description);
        assertEquals(s.containsPaint(), true);
        s.collectPaint();
        // paintCount should be 1
        assertEquals(s.containsPaint(), true);
        s.collectPaint();
        // paintCount should be 0
        assertEquals(s.containsPaint(), false);
        s.collectPaint();
        // paintCount should be 0
        assertEquals(s.containsPaint(), false);
    }

    @Test
    void getPrintableDescriptionReturnsXForNotPassable() {
        JSONObject description = new JSONObject();
        description.put("tileType", 0);
        GridSquare s = new GridSquare(description);
        assertEquals(s.isPassable(), false);
        assertEquals(s.getPrintableDescription(), "x");
    }

    @Test
    void getPrintableDescriptionReturnsColorForPassableWithColor() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        GridSquare s = new GridSquare(description);
        assertEquals(s.isPassable(), true);
        s.setColor("r");
        assertEquals(s.getPrintableDescription(), "r");
    }

    @Test
    void getPrintableDescriptionReturnsPaintCountForPassableWithoutColor() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        description.put("value", 4);
        GridSquare s = new GridSquare(description);
        assertEquals(s.isPassable(), true);
        assertEquals(s.getPrintableDescription(), "4");
    }

    @Test
    void hasColor() {
        JSONObject description = new JSONObject();
        description.put("tileType", 1);
        GridSquare s = new GridSquare(description);
        assertEquals(s.hasColor(), false);
        s.setColor("r");
        assertEquals(s.hasColor(), true);
    }
}