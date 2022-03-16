package org.code.theater.support;

public class PlaySoundAction implements SceneAction {

  private final double[] samples;

  public PlaySoundAction(double[] samples) {
    this.samples = samples;
  }

  public double[] getSamples() {
    return samples;
  }

  @Override
  public SceneActionType getType() {
    return SceneActionType.PLAY_SOUND;
  }
}
