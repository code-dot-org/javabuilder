package org.code.media;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.InternalServerRuntimeError;

public class FontHelper {
  private final Map<String, java.awt.Font> fontMap;

  public FontHelper() {
    this.fontMap = new HashMap<>();
    this.populateFontMap();
  }

  public java.awt.Font getFont(Font font, FontStyle fontStyle) {
    return this.fontMap.get(getFontFilename(font, fontStyle));
  }

  private void populateFontMap() {
    try {
      for (Font font : Font.values()) {
        for (FontStyle fontStyle : FontStyle.values()) {
          String filename = getFontFilename(font, fontStyle);
          InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(filename);
          this.fontMap.put(
              filename, java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fileStream));
        }
      }
    } catch (FontFormatException | IOException e) {
      // throw an exception if we can't load a font, as we will hit a null pointer exception
      // later if the user tries to use that font.
      throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
  }

  private String getFontFilename(Font font, FontStyle fontStyle) {
    return fontToFilenamePrefix.get(font) + fontStyleToFilenameSuffix.get(fontStyle);
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
