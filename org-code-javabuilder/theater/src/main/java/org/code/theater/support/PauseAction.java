package org.code.theater.support;

public class PauseAction implements SceneAction {
  private final double seconds;

  public PauseAction(double seconds) {
    this.seconds = seconds;
  }

  public double getSeconds() {
    return seconds;
  }

  @Override
  public SceneActionType getType() {
    return SceneActionType.PAUSE;
  }
}
