package org.code.playground;

import java.io.FileNotFoundException;
import org.code.media.Image;

public abstract class ClickableImage extends Image {
  private String clickSound;

  public ClickableImage(String filename) throws FileNotFoundException {
    super(filename);
  }

  public ClickableImage(Image source) {
    super(source);
  }

  public ClickableImage(int width, int height) {
    super(width, height);
  }

  public void setClickSound(String filename) throws FileNotFoundException {
    this.clickSound = filename;
  }

  public String getClickSound() {
    return this.clickSound;
  }

  public abstract void onClick();
}
