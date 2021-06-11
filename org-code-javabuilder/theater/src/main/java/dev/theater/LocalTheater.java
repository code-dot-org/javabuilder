package dev.theater;

import java.io.IOException;
import org.code.theater.CatImage;

/** Intended for local testing only. */
public class LocalTheater {
  public static void main(String[] args) throws IOException {
    CatImage image = new CatImage();
    image.buildImageFilter();
    image.buildCanvas();
    image.play();
  }
}
