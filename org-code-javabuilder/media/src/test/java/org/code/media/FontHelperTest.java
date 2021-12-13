package org.code.media;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.code.protocol.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FontHelperTest {
  @BeforeEach
  public void setUp() {
    GlobalProtocol.create(
        mock(OutputAdapter.class),
        mock(InputAdapter.class),
        "",
        "",
        "",
        mock(JavabuilderFileManager.class),
        new CachedResources());
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
