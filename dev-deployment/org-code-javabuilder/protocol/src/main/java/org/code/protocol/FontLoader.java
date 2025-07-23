package org.code.protocol;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FontLoader {
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

  public static Map<String, Font> getFontMap() {
    try {
      Map<String, Font> fontMap = new HashMap<>();
      for (String filename : FONT_FILE_NAMES) {
        String filePath = FONT_FOLDER_NAME + "/" + filename;
        InputStream fileStream = FontLoader.class.getClassLoader().getResourceAsStream(filePath);
        fontMap.put(filename, java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fileStream));
        fileStream.close();
      }
      return fontMap;
    } catch (FontFormatException | IOException e) {
      // throw an exception if we can't load a font, as we will hit a null pointer exception
      // later if the user tries to use that font.
      throw new InternalServerRuntimeException(InternalExceptionKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
  }
}
