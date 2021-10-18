package org.code.theater;

import java.io.FileNotFoundException;
import org.code.media.Image;

public class Prompter {
  protected Prompter() {}

  public Image getImage(String filename) throws FileNotFoundException {
    return new Image(filename);
  }
}
