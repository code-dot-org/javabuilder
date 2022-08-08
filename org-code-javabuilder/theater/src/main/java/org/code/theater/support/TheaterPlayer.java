package org.code.theater.support;

import java.util.List;
import org.code.protocol.JavabuilderSharedObject;

/**
 * Plays a Theater concert with the provided list of {@link SceneAction}s, using a {@link
 * ConcertCreator}. It manages the global Theater state to ensure that a concert is only played once
 * per session. The object should not persist past a single session.
 */
public class TheaterPlayer extends JavabuilderSharedObject {
  // Factory for ease of testing
  static class ConcertCreatorFactory {
    public ConcertCreator create() {
      return new ConcertCreator();
    }
  }

  private final ConcertCreatorFactory concertCreatorFactory;
  private boolean hasPlayed;

  public TheaterPlayer() {
    this(new ConcertCreatorFactory());
  }

  // Visible only for testing
  TheaterPlayer(ConcertCreatorFactory concertCreatorFactory) {
    this.concertCreatorFactory = concertCreatorFactory;
    this.hasPlayed = false;
  }

  public void play(List<SceneAction> actions) {
    if (this.hasPlayed) {
      throw new TheaterRuntimeException(ExceptionKeys.DUPLICATE_PLAY_COMMAND);
    }

    this.hasPlayed = true;
    try (final ConcertCreator concertCreator = this.concertCreatorFactory.create()) {
      concertCreator.publishConcert(actions);
    }
  }
}
