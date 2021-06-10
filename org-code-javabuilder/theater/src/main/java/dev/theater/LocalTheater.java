package dev.theater;

import org.code.theater.CatImage;

/** Intended for local testing only. */
public class LocalTheater {
  public static void main(String[] args) {
    CatImage image = new CatImage(null);
    image.buildImageFilter();
    image.buildCanvas();
  }
}
