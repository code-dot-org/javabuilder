package org.code.theater.support;

import org.code.media.Color;

public class ClearSceneAction implements SceneAction {
  private final Color color;

  public ClearSceneAction(Color color) {
    this.color = color;
  }

  public Color getColor() {
    return color;
  }

  @Override
  public SceneActionType getType() {
    return SceneActionType.CLEAR_SCENE;
  }
}
