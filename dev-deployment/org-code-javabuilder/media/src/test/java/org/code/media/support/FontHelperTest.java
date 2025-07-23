package org.code.media.support;

import static org.junit.jupiter.api.Assertions.*;

import org.code.media.Font;
import org.code.media.FontStyle;
import org.code.protocol.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FontHelperTest {
  @BeforeEach
  public void setUp() {
    CachedResources.create();
  }

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
