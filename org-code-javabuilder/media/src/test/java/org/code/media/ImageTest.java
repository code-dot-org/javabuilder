package org.code.media;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.code.protocol.GlobalProtocol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class ImageTest {
  MockedStatic<GlobalProtocol> globalProtocol;
  GlobalProtocol globalProtocolInstance;

  @BeforeEach
  public void setUp() {
    globalProtocol = mockStatic(GlobalProtocol.class);
    globalProtocolInstance = mock(GlobalProtocol.class);
    globalProtocol.when(GlobalProtocol::getInstance).thenReturn(globalProtocolInstance);
  }

  @AfterEach
  public void tearDown() {
    globalProtocol.close();
  }

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

  @Test
  public void getImageMaintainsSizeIf400Square() throws FileNotFoundException {
    String imageFileName = "400x400Image.jpg";
    String testFileURL =
        Thread.currentThread().getContextClassLoader().getResource(imageFileName).toString();
    when(globalProtocolInstance.generateAssetUrl(imageFileName)).thenReturn(testFileURL);

    BufferedImage image = Image.getImageAssetFromFile(imageFileName);
    assertEquals(400, image.getHeight());
    assertEquals(400, image.getWidth());
  }

  @Test
  public void getImageMaintainsSizeIfSmall() throws FileNotFoundException {
    String imageFileName = "200w300h.png";
    String testFileURL =
        Thread.currentThread().getContextClassLoader().getResource(imageFileName).toString();
    when(globalProtocolInstance.generateAssetUrl(imageFileName)).thenReturn(testFileURL);

    BufferedImage image = Image.getImageAssetFromFile(imageFileName);
    assertEquals(300, image.getHeight());
    assertEquals(200, image.getWidth());
  }

  @Test
  public void getImageResizesIfWide() throws FileNotFoundException {
    String imageFileName = "600w300h.png";
    String testFileURL =
        Thread.currentThread().getContextClassLoader().getResource(imageFileName).toString();
    when(globalProtocolInstance.generateAssetUrl(imageFileName)).thenReturn(testFileURL);

    BufferedImage image = Image.getImageAssetFromFile(imageFileName);
    assertEquals(200, image.getHeight());
    assertEquals(400, image.getWidth());
  }

  @Test
  public void getImageResizesIfTall() throws FileNotFoundException {
    String imageFileName = "200w800h.png";
    String testFileURL =
        Thread.currentThread().getContextClassLoader().getResource(imageFileName).toString();
    when(globalProtocolInstance.generateAssetUrl(imageFileName)).thenReturn(testFileURL);

    BufferedImage image = Image.getImageAssetFromFile(imageFileName);
    assertEquals(400, image.getHeight());
    assertEquals(100, image.getWidth());
  }

  @Test
  public void getImageThrowsIOExceptionIfFilenameInvalid() {
    String imageFileName = "notHere.png";
    Exception exception =
        assertThrows(
            IOException.class,
            () -> {
              Image.getImageAssetFromFile(imageFileName);
            });
    assertEquals(exception.getMessage(), (imageFileName));
  }
}
