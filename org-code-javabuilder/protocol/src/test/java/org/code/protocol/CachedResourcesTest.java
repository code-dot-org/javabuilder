package org.code.protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class CachedResourcesTest {
  @Test
  public void createsFontMap() {
    CachedResources cachedResources = new CachedResources();
    Map<String, Font> fonts = cachedResources.getFontMap();
    // there should be 12 fonts in the map
    assertEquals(12, fonts.keySet().size());
  }

  @Test
  public void onlyCreatesFontMapOnce() {
    CachedResources cachedResources = new CachedResources();
    Map<String, Font> fonts = cachedResources.getFontMap();
    Font monoBold = fonts.get("LiberationMono-Bold.ttf");
    Map<String, Font> fontsAgain = cachedResources.getFontMap();
    Font secondMonoBold = fonts.get("LiberationMono-Bold.ttf");
    // assert font is not null
    assertNotNull(monoBold);
    // assert object equality of the two "mono bold" fonts.
    assertTrue(monoBold == secondMonoBold);
  }
}
