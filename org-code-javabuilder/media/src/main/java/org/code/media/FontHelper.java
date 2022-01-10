package org.code.media;

import java.awt.*;
import java.util.Map;
import org.code.protocol.CachedResources;

public class FontHelper {
  private final Map<String, java.awt.Font> fontMap;

  public FontHelper() {
    this.fontMap = CachedResources.getInstance().getFontMap();
  }

  public java.awt.Font getFont(Font font, FontStyle fontStyle) {
    return this.fontMap.get(getFontFilename(font, fontStyle));
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
