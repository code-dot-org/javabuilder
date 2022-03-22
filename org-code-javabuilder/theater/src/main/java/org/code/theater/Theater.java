package org.code.theater;

import java.util.ArrayList;
import java.util.List;
import org.code.theater.support.SceneAction;
import org.code.theater.support.TheaterPlayer;

public final class Theater {
  public static final Stage stage = new Stage();
  public static final Prompter prompter = new Prompter();

  public static void playScene(Scene scene) {
    Theater.playScenes(scene);
  }

  public static void playScenes(Scene... scenes) {
    final List<SceneAction> allActions = new ArrayList<>();
    for (Scene scene : scenes) {
      allActions.addAll(scene.getActions());
    }

    TheaterPlayer.getInstance().play(allActions);
  }

  public static void play(Scene[] scenes) {
    Theater.playScenes(scenes);
  }
}
