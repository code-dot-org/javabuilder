package dev.theater;

import org.code.theater.CatImage;

/** Intended for local testing only. */
public class LocalTheater {
  public static void main(String[] args) {
    CatImage image = new CatImage();
    image.buildImageFilter(0);
    image.buildCanvas(1000);
    image.play();
  }
}
