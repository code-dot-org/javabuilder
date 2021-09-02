package org.code.media;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FontHelper {
  private static Font font;

  public FontHelper() {
    try {
      System.out.println("creating file...");
      //      InputStream fontStream =
      //          this.getClass().getClassLoader().getResourceAsStream("LiberationSans-Bold.ttf");
      File fontFile = new File("/opt/fonts/LiberationSans-Bold.ttf");
      System.out.println("file length: " + fontFile.length());
      this.font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
    } catch (FontFormatException e) {
      System.out.println("font format exception: " + e);
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("io exception: " + e);
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println("some other exception: " + e);
      e.printStackTrace();
    }
    boolean isNull = this.font == null;
    System.out.println("finishing font helper constructor. is null is " + isNull);
  }

  public Font getFont() {
    System.out.println("in getFont");
    System.out.println("font name: " + this.font.getFontName());
    return this.font;
  }
}
