package org.code.media;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.code.media.support.MediaRuntimeException;
import org.code.media.support.MediaRuntimeExceptionKeys;
import org.code.protocol.GlobalProtocol;

public class Image {
  private Pixel[][] pixels;
  private int width;
  private int height;
  private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
  private BufferedImage bufferedImage;
  private static final int MAX_WIDTH = 400;
  private static final int MAX_HEIGHT = 400;

  /**
   * Creates a new image object, using the pixel information from the file uploaded to the asset
   * manager.
   *
   * @param filename the name of the image loaded into the asset manager for the project
   */
  public Image(String filename) {
    try {
      this.bufferedImage = Image.getImageAssetFromFile(filename);
    } catch (FileNotFoundException e) {
      throw new MediaRuntimeException(MediaRuntimeExceptionKeys.FILE_NOT_FOUND, e);
    }
    this.width = bufferedImage.getWidth();
    this.height = bufferedImage.getHeight();
    // don't create pixel array until we need it
    this.pixels = null;
  }

  /**
   * Create a new image object, copying the source image provided.
   *
   * @param source the image to duplicate
   */
  public Image(Image source) {
    this.width = source.getWidth();
    this.height = source.getHeight();
    this.pixels = new Pixel[this.width][this.height];
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++) {
        Color sourceColor = source.getPixel(x, y).getColor();
        this.pixels[x][y] = new Pixel(this, x, y, new Color(sourceColor));
      }
    }
  }

  /**
   * Creates an empty image filled with the default background color.
   *
   * @param width the width of the image to create.
   * @param height the height of the image to create.
   */
  public Image(int width, int height) {
    this.pixels = new Pixel[width][height];
    this.width = width;
    this.height = height;
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++) {
        this.pixels[x][y] = new Pixel(this, x, y, DEFAULT_BACKGROUND_COLOR);
      }
    }
  }

  /**
   * Creates a new Image from the given BufferedImage. Only meant for internal use.
   *
   * @param bufferedImage BufferedImage that backs this Image object
   */
  private Image(BufferedImage bufferedImage) {
    this.bufferedImage = bufferedImage;
    this.width = bufferedImage.getWidth();
    this.height = bufferedImage.getHeight();
    // don't create pixel array until we need it
    this.pixels = null;
  }

  /**
   * Get the Pixel at the (x,y) coordinate specified.
   *
   * @param x the x position of the pixel
   * @param y the y position of the pixel
   * @return Pixel at the given coordinate
   */
  public Pixel getPixel(int x, int y) {
    if (this.pixels == null) {
      this.createPixelArray();
      this.bufferedImage = null;
    }
    return this.pixels[x][y];
  }

  /**
   * Set the Pixel at the (x,y) coordinate specified.
   *
   * @param x the x position of the pixel
   * @param y the y position of the pixel
   * @param color the color to set the pixel
   */
  public void setPixel(int x, int y, Color color) {
    if (this.pixels == null) {
      this.createPixelArray();
      this.bufferedImage = null;
    }
    this.pixels[x][y].setColor(color);
  }

  /**
   * Gets the width of the image in pixels.
   *
   * @return the width of the image in pixels.
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * Gets the height of the image in pixels.
   *
   * @return the height of the image in pixels.
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * Clears the image, filling it with the color provided.
   *
   * @param color the color with which to fill the image.
   */
  public void clear(Color color) {
    if (this.pixels == null) {
      this.pixels = new Pixel[this.width][this.height];
      this.bufferedImage = null;
    }
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++) {
        this.pixels[x][y].setColor(color);
      }
    }
  }

  /**
   * Get a BufferedImage of this Image
   *
   * @return
   */
  public BufferedImage getBufferedImage() {
    // if we've never set up the pixel array, return our buffered image
    if (this.pixels == null) {
      return this.bufferedImage;
    }
    BufferedImage bufferedImageResult =
        new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++) {
        bufferedImageResult.setRGB(x, y, this.pixels[x][y].getColor().getRGB());
      }
    }
    return bufferedImageResult;
  }

  /**
   * Load the given image asset from file and return it as a BufferedImage
   *
   * @param filename
   * @return BufferedImage
   * @throws FileNotFoundException if the file is not found
   */
  public static BufferedImage getImageAssetFromFile(String filename) throws FileNotFoundException {
    try {
      return Image.getImageFromUrl(
          new URL(GlobalProtocol.getInstance().getContentManager().getAssetUrl(filename)));
    } catch (IOException e) {
      throw new FileNotFoundException(filename);
    }
  }

  private static BufferedImage getImageFromUrl(URL url) throws FileNotFoundException {
    BufferedImage originalImage;
    try {
      originalImage = ImageIO.read(url);
      if (originalImage == null) {
        // this can happen if the URL is not associated with an image
        throw new MediaRuntimeException(MediaRuntimeExceptionKeys.IMAGE_LOAD_ERROR);
      }
    } catch (IOException e) {
      throw new FileNotFoundException(url.toString());
    }

    // Resize the image to its max size while maintaining the aspect ratio.
    // This allows us to save time when students run on pixel manipulation on an entire image.
    int height = originalImage.getHeight();
    int width = originalImage.getWidth();
    if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
      return originalImage;
    }
    int targetHeight, targetWidth;
    if (height > width) {
      targetHeight = MAX_HEIGHT;
      targetWidth = (int) ((double) width / height * MAX_HEIGHT);
    } else {
      targetWidth = MAX_WIDTH;
      targetHeight = (int) ((double) height / width * MAX_WIDTH);
    }
    BufferedImage image = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics2D = image.createGraphics();
    graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
    graphics2D.dispose();

    return image;
  }

  /** Create a 2d array of pixels from this.bufferedImage */
  private void createPixelArray() {
    this.pixels = new Pixel[this.width][this.height];
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++) {
        int rgbColor = this.bufferedImage.getRGB(x, y);
        this.pixels[x][y] = new Pixel(this, x, y, new Color(rgbColor));
      }
    }
  }
}
