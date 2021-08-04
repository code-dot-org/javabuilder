package org.code.media;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ColorTest {
  @Test
  public void defaultAlphaIs255() {
    Color c = new Color(0, 0, 0);
    assertEquals(255, c.getAlpha());
  }

  @Test
  public void resettingColorSetsAlpha() {
    Color c = new Color(0, 0, 0, 0);
    assertEquals(0, c.getAlpha());
    c.setBlue(255);
    // now alpha should be 255 (max opacity), since we set the blue value on the color
    assertEquals(255, c.getAlpha());
  }
}
