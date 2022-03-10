package org.code.theater;

import org.code.media.Color;

public static final class Theater {
  public static final Prompter prompter;
  
  /** Plays the scene provided.
   *
   * @param scene the scene to play
   */
  public static void playScene(Scene scene);

  /** Plays the provided array of scenes in order.
   *
   * @param scenes the array of scenes to play
   */
  public static void playScenes(Scene[] scenes);
}
