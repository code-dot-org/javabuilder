package org.code.theater;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.OutputAdapter;

/**
 * This is a sample library with some image manipulation that shows how theater could use the
 * framework that has been set up. This is for demo purposes only. No production code should be
 * added here and the class should be deleted before we ship Theater.
 */
public class CatImage {
  private final OutputAdapter outputAdapter;

  public CatImage() {
    this.outputAdapter = GlobalProtocol.getInstance().getOutputAdapter();
  }

  public CatImage(OutputAdapter outputAdapter) {
    this.outputAdapter = outputAdapter;
  }

  public void buildImageFilter() {
    ImagePlus image = null;
    try {
      image =
          IJ.openImage(
              Paths.get(CatImage.class.getClassLoader().getResource("sampleImageBeach.jpg").toURI())
                  .toString());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    // Uncommenting the following line and setting it to a local directory will save the image to
    // your local computer
    // IJ.save(image, "<my local directory>\beach.gif");

    ImageProcessor ip = image.getProcessor();
    BufferedImage bufferedImage = (BufferedImage) ip.createImage();
    outputAdapter.sendMessage(ImageEncoder.encodeImageToMessage(bufferedImage));
  }

  public void buildCanvas() {
    ImagePlus image = IJ.createImage("test", 300, 150, 1, 24);
    ImageProcessor ip = image.getProcessor();

    // Draw rectangle
    ip.setLineWidth(10);
    ip.setColor(Color.BLUE);
    ip.fillRect(50, 50, 100, 100);

    // Draw square
    int[] x = {10, 40, 25};
    int[] y = {10, 10, 30};
    ip.setColor(Color.RED);
    ip.fillPolygon(new Polygon(x, y, 3));

    // Draw circle
    ip.setColor(Color.GREEN);
    ip.fillOval(30, 20, 50, 30);

    // Draw text
    ip.setFontSize(30);
    ip.setColor(Color.YELLOW);
    ip.drawString("Hello World!", 100, 90);

    // Uncommenting the following line and setting it to a local directory will save the image to
    // your local computer
    // IJ.save(image, "<my local directory>\beach.gif");

    BufferedImage bufferedImage = (BufferedImage) ip.createImage();
    outputAdapter.sendMessage(ImageEncoder.encodeImageToMessage(bufferedImage));
  }
}
