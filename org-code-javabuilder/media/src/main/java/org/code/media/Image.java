package org.code.media;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.code.protocol.GlobalProtocol;

public class Image {
  private Pixel[][] pixels;
  private int width;
  private int height;
  private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;

  /**
   * Creates a new image object, using the pixel information from the file uploaded to the asset
   * manager.
   *
   * @param filename the name of the image loaded into the asset manager for the project
   * @throws FileNotFoundException if the file doesn't exist in the asset manager.
   */
  public Image(String filename) throws FileNotFoundException {
    BufferedImage bufferedImage = Image.getImageAssetFromFile(filename);
    this.width = bufferedImage.getWidth();
    this.height = bufferedImage.getHeight();
    this.pixels = new Pixel[this.width][this.height];

    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++) {
        int rgbColor = bufferedImage.getRGB(x, y);
        this.pixels[x][y] = new Pixel(this, x, y, new Color(rgbColor));
      }
    }
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
   * Get the Pixel at the (x,y) coordinate specified.
   *
   * @param x the x position of the pixel
   * @param y the y position of the pixel
   * @return Pixel at the given coordinate
   */
  public Pixel getPixel(int x, int y) {
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
    BufferedImage bufferedImage =
        new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++) {
        bufferedImage.setRGB(x, y, this.pixels[x][y].getColor().getRGB());
      }
    }
    return bufferedImage;
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
      BufferedImage image =
          ImageIO.read(new URL(GlobalProtocol.getInstance().generateAssetUrl(filename)));
      if (image == null) {
        // this can happen if the filename is not associated with an image
        throw new MediaRuntimeException(MediaRuntimeExceptionKeys.IMAGE_LOAD_ERROR);
      }
      return image;
    } catch (IOException e) {
      throw new FileNotFoundException("Could not find file " + filename);
    }
  }
}
