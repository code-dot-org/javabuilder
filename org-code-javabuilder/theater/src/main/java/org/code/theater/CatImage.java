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
    // Challenges:
    // * Getting font working
    // * Keeping the gif size _small_
    // * API gateway supports 128KB messages with websockets and 10MB with HTTP
    // * We'll need to upload to S3 and access from there.
    // * For fonts, we'll need to either re-package our lambda as a docker image with libfontconfig1 installed
    //   or install our own fonts. https://stackoverflow.com/questions/61024955/how-do-i-configure-simple-java-fontconfig-properties-file-for-use-on-linux
    final Canvas canvas = new Canvas()
    {
        public void paint(Graphics g)
        {
            Rectangle r = getBounds();
            g.drawLine(0, 0, r.width - 1, r.height - 1);
            // Colors work too.
            g.setColor(new Color(255, 127, 0));
            g.drawLine(0, r.height - 1, r.width - 1, 0);
            // // And fonts
            // GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            // String fonts[] = ge.getAvailableFontFamilyNames();
            // System.out.println(fonts[0]);
            // g.setFont(new Font(fonts[0], Font.ITALIC, 30));
            // g.drawString("Test", 100, 100);
        }
    };
    // And all the operations work correctly.
    canvas.setBounds(32, 32, 400, 400);
    BufferedImage image=new BufferedImage(canvas.getWidth(), canvas.getHeight(),BufferedImage.TYPE_INT_RGB);
    Graphics2D g2=(Graphics2D)image.getGraphics();
    canvas.paint(g2);

    this.gifWriter.writeToGif(image, delay);

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
