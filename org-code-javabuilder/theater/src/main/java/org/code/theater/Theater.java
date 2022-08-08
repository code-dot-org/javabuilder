package org.code.theater;

import java.util.ArrayList;
import java.util.List;
import org.code.protocol.JavabuilderContext;
import org.code.theater.support.SceneAction;
import org.code.theater.support.TheaterPlayer;

public final class Theater {
  public static final Prompter prompter = new Prompter();

  public static void playScenes(Scene scene) {
    Theater.playInternal(scene);
  }

  public static void playScenes(Scene... scenes) {
    Theater.playInternal(scenes);
  }

  public static void play(Scene[] scenes) {
    Theater.playInternal(scenes);
  }

  private static void playInternal(Scene... scenes) {
    final List<SceneAction> allActions = new ArrayList<>();
    for (Scene scene : scenes) {
      allActions.addAll(scene.getActions());
    }
    JavabuilderContext context = JavabuilderContext.getInstance();
    if (!context.containsKey(TheaterPlayer.class)) {
      context.register(TheaterPlayer.class, new TheaterPlayer());
    }
    TheaterPlayer theaterPlayer =
        (TheaterPlayer) JavabuilderContext.getInstance().get(TheaterPlayer.class);
    theaterPlayer.play(allActions);
  }
}
