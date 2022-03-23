package org.code.theater.support;

import java.util.List;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.LifecycleListener;

/**
 * Plays a Theater concert with the provided list of {@link SceneAction}s, using a {@link
 * ConcertCreator}. As this class is only meant to be used as a singleton, it manages the global
 * Theater state to ensure that a concert is only played once per session, and that the flag is
 * reset when execution ends.
 */
public class TheaterPlayer implements LifecycleListener {
  private static final TheaterPlayer instance = new TheaterPlayer();

  public static TheaterPlayer getInstance() {
    return instance;
  }

  // Factory for ease of testing
  static class ConcertCreatorFactory {
    public ConcertCreator create() {
      return new ConcertCreator();
    }
  }

  private final ConcertCreatorFactory concertCreatorFactory;
  private boolean hasPlayed;

  private TheaterPlayer() {
    this(new ConcertCreatorFactory());
  }

  // Visible only for testing
  TheaterPlayer(ConcertCreatorFactory concertCreatorFactory) {
    this.concertCreatorFactory = concertCreatorFactory;
    this.hasPlayed = false;
    GlobalProtocol.getInstance().registerLifecycleListener(this);
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

  // Temporary method while we support both versions of the Theater API (Stage and Scene)
  // to ensure that only one play command is called per project. When Stage is deleted,
  // remove this method.
  public void onStagePlay() {
    if (this.hasPlayed) {
      throw new TheaterRuntimeException(ExceptionKeys.DUPLICATE_PLAY_COMMAND);
    }

    this.hasPlayed = true;
  }

  @Override
  public void onExecutionEnded() {
    this.hasPlayed = false;
  }
}
