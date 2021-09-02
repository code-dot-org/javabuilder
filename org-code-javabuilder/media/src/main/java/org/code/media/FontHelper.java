package org.code.media;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FontHelper {
  private final Map<String, java.awt.Font> fontMap;
  private static final String FONT_FOLDER = "/opt/fonts/";

  public FontHelper() {
    this.fontMap = new HashMap<>();
    //    Properties props = System.getProperties();
    //    props.put("sun.awt.fontconfig", FONT_FOLDER + "fontconfig.properties");
    this.populateFontMap();
  }

  public java.awt.Font getFont(Font font, FontStyle fontStyle) {
    return this.fontMap.get(
        fontToFilenamePrefix.get(font) + fontStyleToFilenameSuffix.get(fontStyle));
  }

  private void populateFontMap() {
    try {
      for (Font font : Font.values()) {
        for (FontStyle fontStyle : FontStyle.values()) {
          String filename =
              fontToFilenamePrefix.get(font) + fontStyleToFilenameSuffix.get(fontStyle);
          InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(filename);
          this.fontMap.put(
              filename, java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fileStream));
        }
      }
    } catch (FontFormatException e) {
      // TODO: throw exception
      e.printStackTrace();
    } catch (IOException e) {
      // TODO: throw exception
      e.printStackTrace();
    }
  }

  private static Map<Font, String> fontToFilenamePrefix =
      Map.ofEntries(
          Map.entry(Font.MONO, "LiberationMono"),
          Map.entry(Font.SANS, "LiberationSans"),
          Map.entry(Font.SERIF, "LiberationSerif"));

  private static Map<FontStyle, String> fontStyleToFilenameSuffix =
      Map.ofEntries(
          Map.entry(FontStyle.NORMAL, "-Regular.ttf"),
          Map.entry(FontStyle.BOLD, "-Bold.ttf"),
          Map.entry(FontStyle.ITALIC, "-Italic.ttf"),
          Map.entry(FontStyle.BOLD_ITALIC, "-BoldItalic.ttf"));
}
