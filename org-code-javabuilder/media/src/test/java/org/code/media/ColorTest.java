package org.code.media;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ColorTest {
  @Test
  public void defaultAlphaIs255() {
    Color c = new Color(0, 0, 0);
    assertEquals(255, c.getAlpha());
  }

  @Test
  public void testCopyWithRedCreatesCopyWithNewRedValue() {
    final int red = 100;
    final int green = 150;
    final int blue = 200;
    final int newRed = 50;
    final Color copied = Color.copyWithRed(new Color(red, green, blue), newRed);
    assertEquals(newRed, copied.getRed());
    assertEquals(green, copied.getGreen());
    assertEquals(blue, copied.getBlue());
  }

  @Test
  public void testCopyWithGreenCreatesCopyWithNewGreenValue() {
    final int red = 100;
    final int green = 150;
    final int blue = 200;
    final int newGreen = 50;
    final Color copied = Color.copyWithGreen(new Color(red, green, blue), newGreen);
    assertEquals(red, copied.getRed());
    assertEquals(newGreen, copied.getGreen());
    assertEquals(blue, copied.getBlue());
  }

  @Test
  public void testCopyWithBlueCreatesCopyWithNewBlueValue() {
    final int red = 100;
    final int green = 150;
    final int blue = 200;
    final int newBlue = 50;
    final Color copied = Color.copyWithBlue(new Color(red, green, blue), newBlue);
    assertEquals(red, copied.getRed());
    assertEquals(green, copied.getGreen());
    assertEquals(newBlue, copied.getBlue());
  }
}
