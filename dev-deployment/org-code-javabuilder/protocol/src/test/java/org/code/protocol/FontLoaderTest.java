package org.code.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class FontLoaderTest {
  @Test
  public void createsFontMap() {
    Map<String, Font> fonts = FontLoader.getFontMap();
    // there should be 12 fonts in the map
    assertEquals(12, fonts.keySet().size());
  }
}
