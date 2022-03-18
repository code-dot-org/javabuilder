package org.code.theater.support;

import org.code.theater.Instrument;

public class PlayNoteAction implements SceneAction {

  private final Instrument instrument;
  private final int note;
  private final double seconds;

  public PlayNoteAction(Instrument instrument, int note, double seconds) {
    this.instrument = instrument;
    this.note = note;
    this.seconds = seconds;
  }

  public Instrument getInstrument() {
    return instrument;
  }

  public int getNote() {
    return note;
  }

  public double getSeconds() {
    return seconds;
  }

  @Override
  public SceneActionType getType() {
    return SceneActionType.PLAY_NOTE;
  }
}
