package org.code.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

public class ImageTest {
  @Test
  public void createsEmptyImageCorrectly() {
    Image test = new Image(200, 200);
    assertEquals(Color.WHITE, test.getPixel(0, 0).getColor());
  }

  @Test
  public void canCopyImage() {
    Image test1 = new Image(200, 200);
    Image test2 = new Image(test1);
    test2.setPixel(0, 0, Color.AQUA);
    // assert pixels are not the same for different images
    assertNotEquals(test1.getPixel(0, 0), test2.getPixel(0, 0));
    assertEquals(test1.getPixel(0, 0).getColor(), Color.WHITE);
    assertEquals(test2.getPixel(0, 0).getColor(), Color.AQUA);
  }

  @Test
  public void canSetColorViaPixel() {
    Image test = new Image(200, 200);
    test.getPixel(5, 10).setColor(Color.BLUE);
    assertEquals(Color.BLUE, test.getPixel(5, 10).getColor());
  }

  @Test
  public void canModifyColorAndItModifiesPixel() {
    Image test = new Image(500, 300);
    Color c = test.getPixel(50, 200).getColor();
    c.setBlue(50);
    assertEquals(50, test.getPixel(50, 200).getBlue());
  }

  @Test
  public void canCreateBufferedImage() {
    Image test = new Image(500, 300);
    // set one pixel to a specific color
    test.setPixel(200, 100, new Color(50, 34, 25));
    BufferedImage bufferedImage = test.getBufferedImage();
    java.awt.Color pixelColor = new java.awt.Color(bufferedImage.getRGB(200, 100));
    // verify that pixel is set in the buffered image
    assertEquals(50, pixelColor.getRed());
    assertEquals(34, pixelColor.getGreen());
    assertEquals(25, pixelColor.getBlue());
  }
}
