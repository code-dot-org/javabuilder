package org.code.theater;

import org.code.media.Color;

public static final class Theater {
  public static final Prompter prompter;

  /** Returns the width of the theater canvas. */
  public static int getWidth();

  /** Returns the height of the theater canvas. */
  public static  int getHeight();

  /**
   * Wait the provided number of seconds before performing the next draw or play command.
   *
   * @param seconds The number of seconds to wait. This can be a fraction of a second, but the
   *     smallest value can be .1 seconds.
   */
  public static void pause(double seconds);

  /**
   * Clear the canvas and set the background to the given color name.
   *
   * @param color new background color name. If the name does not match a known color
   *    or hex value, this call will set the background to black.
   */
  public static void clear(String color);

  /**
   * Clear the canvas and set the background to the given color
   *
   * @param color new background color
   */
  public static void clear(Color color);
  
  /** Plays the instructions. */
  public static void play();
}
