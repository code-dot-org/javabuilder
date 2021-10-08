package org.code.media;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PixelTest {

  private Image image;

  @BeforeEach
  public void setUp() {
    image = mock(Image.class);
  }

  @Test
  public void testColorSettersUpdateColor() {
    final Color original = new Color(50, 150, 250);
    final Pixel pixel = new Pixel(image, 0, 0, original);

    final int newValue = 123;
    pixel.setRed(newValue);
    assertEquals(newValue, pixel.getRed());

    pixel.setGreen(newValue);
    assertEquals(newValue, pixel.getGreen());

    pixel.setBlue(newValue);
    assertEquals(newValue, pixel.getBlue());

    final Color newColor = new Color(123, 234, 111);
    pixel.setColor(newColor);
    assertEquals(newColor.getRed(), pixel.getRed());
    assertEquals(newColor.getGreen(), pixel.getGreen());
    assertEquals(newColor.getBlue(), pixel.getBlue());
  }
}
