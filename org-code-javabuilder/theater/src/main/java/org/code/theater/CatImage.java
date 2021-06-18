package org.code.theater;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.OutputAdapter;

import javax.imageio.ImageIO;

/**
 * This is a sample library with some image manipulation that shows how theater could use the
 * framework that has been set up. This is for demo purposes only. No production code should be
 * added here and the class should be deleted before we ship Theater.
 */
public class CatImage {
  private final OutputAdapter outputAdapter;
  private final GifWriter gifWriter;
  private final ByteArrayOutputStream outputStream;

  public CatImage() {
    this(GlobalProtocol.getInstance().getOutputAdapter());
  }

  public CatImage(OutputAdapter outputAdapter) {
    this.outputStream = new ByteArrayOutputStream();
    this.gifWriter = new GifWriter(this.outputStream);
    this.outputAdapter = outputAdapter;
    System.setProperty("java.awt.headless", "true");
  }

  public void buildImageFilter(int delay) {
    Toolkit tk = Toolkit.getDefaultToolkit();
    File f = null;
    BufferedImage image = null;
    try {
      f = new File(Paths.get(CatImage.class.getClassLoader().getResource("sampleImageBeach.jpg").toURI()).toString());
      image = ImageIO.read(f);
    } catch (Exception e) {
      e.printStackTrace();
    }
//    image.c

    // Uncommenting the following line and setting it to a local directory will save the image to
    // your local computer
    // IJ.save(image, "<my local directory>\beach.gif");

//    ImageProcessor ip = image.getProcessor();
//    BufferedImage bufferedImage = (BufferedImage) image;
    this.gifWriter.writeToGif(image, delay);
  }

  // Play the created gif (close gif writer and send message).
  public void play() {
    this.gifWriter.close();
    outputAdapter.sendMessage(ImageEncoder.encodeStreamToMessage(this.outputStream));
  }

  public void buildCanvas(int delay) {
//    Canvas canvas = new Canvas();
//    Image image = canvas.createImage(300, 300);
//    image.
//    Graphics ip = image.getGraphics();
////    ImagePlus image = IJ.createImage("test", 300, 150, 1, 24);
////    ImageProcessor ip = image.getProcessor();
//
//    // Draw rectangle
////    ip.setLineWidth(10);
//    ip.setColor(Color.BLUE);
//    ip.fillRect(50, 50, 100, 100);
//
//    // Draw square
//    int[] x = {10, 40, 25};
//    int[] y = {10, 10, 30};
//    ip.setColor(Color.RED);
//    ip.fillPolygon(new Polygon(x, y, 3));
//
//    // Draw circle
//    ip.setColor(Color.GREEN);
//    ip.fillOval(30, 20, 50, 30);
//
//    // Draw text
////    ip.setFontSize(30);
//    ip.setFont(new Font("Arial", Font.ITALIC, 12));
//    ip.drawString("Test", 32, 8);
//
//    // Uncommenting the following line and setting it to a local directory will save the image to
//    // your local computer
//    // IJ.save(image, "<my local directory>\beach.gif");
//
////    BufferedImage bufferedImage = (BufferedImage) ip.createImage();
//    this.gifWriter.writeToGif((BufferedImage)image, delay);
  }
}
