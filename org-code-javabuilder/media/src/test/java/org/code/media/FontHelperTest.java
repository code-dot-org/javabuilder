package org.code.media;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FontHelperTest {
  @Test
  void loadsAllFonts() {
    FontHelper fontHelper = new FontHelper();
    for (Font font : Font.values()) {
      for (FontStyle fontStyle : FontStyle.values()) {
        assertNotNull(fontHelper.getFont(font, fontStyle));
      }
    }
  }
}
