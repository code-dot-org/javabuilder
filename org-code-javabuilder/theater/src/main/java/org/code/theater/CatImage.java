package org.code.theater;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.OutputAdapter;

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
      f =
          new File(
              Paths.get(CatImage.class.getClassLoader().getResource("sampleImageBeach.jpg").toURI())
                  .toString());
      image = ImageIO.read(f);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Uncommenting the following line and setting it to a local directory will save the image to
    // your local computer
    // IJ.save(image, "<my local directory>\beach.gif");

    this.gifWriter.writeToGif(image, delay);
  }

  // Play the created gif (close gif writer and send message).
  public void play() {
    this.gifWriter.close();
    outputAdapter.sendMessage(ImageEncoder.encodeStreamToMessage(this.outputStream));
  }

  public void buildCanvas(int delay) {
    final Canvas canvas =
        new Canvas() {
          public void paint(Graphics g) {
            // Draw rectangle
            Rectangle r = getBounds();
            // Draw line sample.
            g.drawLine(0, 0, r.width - 1, r.height - 1);
            g.setColor(new Color(255, 127, 0));
            g.drawLine(0, r.height - 1, r.width - 1, 0);
            // For fonts, we'll need to either re-package our lambda as a docker image with
            // libfontconfig1 installed or install our own fonts. This will not work as-is.
            // https://stackoverflow.com/questions/61024955/how-do-i-configure-simple-java-fontconfig-properties-file-for-use-on-linux
            // GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            // String fonts[] = ge.getAvailableFontFamilyNames();
            // System.out.println(fonts[0]);
            // g.setFont(new Font(fonts[0], Font.ITALIC, 30));
            // g.drawString("Test", 100, 100);
          }
        };

    canvas.setBounds(32, 32, 400, 400);
    BufferedImage image =
        new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = (Graphics2D) image.getGraphics();
    canvas.paint(g2);

    this.gifWriter.writeToGif(image, delay);
  }
}
