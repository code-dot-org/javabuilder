package org.code.media;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class FontHelper {
  private static Font font;

  public FontHelper() {
    try {
      InputStream fontStream =
          this.getClass().getClassLoader().getResourceAsStream("LiberationSans-Bold.ttf");
      this.font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
    } catch (FontFormatException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Font getFont() {
    return this.font;
  }
}
