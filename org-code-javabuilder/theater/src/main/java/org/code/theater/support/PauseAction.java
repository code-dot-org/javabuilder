package org.code.theater.support;

public class PauseAction implements SceneAction {
  private final double seconds;

  public PauseAction(double seconds) {
    if (!Double.isFinite(seconds)) {
      throw new IllegalArgumentException("Pause seconds must be finite");
    }
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
