package org.code.protocol;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CachedResources {
  private Map<String, Font> fontMap;

  private static final String FONT_FOLDER_NAME = "fonts";
  private static final String[] FONT_FILE_NAMES =
      new String[] {
        "LiberationMono-Bold.ttf",
        "LiberationMono-BoldItalic.ttf",
        "LiberationMono-Italic.ttf",
        "LiberationMono-Regular.ttf",
        "LiberationSans-Bold.ttf",
        "LiberationSans-BoldItalic.ttf",
        "LiberationSans-Italic.ttf",
        "LiberationSans-Regular.ttf",
        "LiberationSerif-Bold.ttf",
        "LiberationSerif-BoldItalic.ttf",
        "LiberationSerif-Italic.ttf",
        "LiberationSerif-Regular.ttf"
      };

  public Map<String, Font> getFontMap() {
    if (this.fontMap == null) {
      try {
        this.fontMap = new HashMap<>();
        for (String filename : FONT_FILE_NAMES) {
          String filePath = FONT_FOLDER_NAME + "/" + filename;
          InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
          this.fontMap.put(
              filename, java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fileStream));
          fileStream.close();
        }
        Logger.getLogger(MAIN_LOGGER).info("created font map");
      } catch (FontFormatException | IOException e) {
        // throw an exception if we can't load a font, as we will hit a null pointer exception
        // later if the user tries to use that font.
        throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
      }
    }
    return this.fontMap;
  }
}
