package org.code.theater.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import org.code.protocol.GlobalProtocolTestFactory;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.InternalServerRuntimeError;
import org.code.protocol.LifecycleNotifier;
import org.code.theater.support.TheaterPlayer.ConcertCreatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TheaterPlayerTest {

  private ConcertCreator concertCreator;
  private LifecycleNotifier lifecycleNotifier;
  private List<SceneAction> actions;
  private TheaterPlayer unitUnderTest;

  @BeforeEach
  public void setUp() {
    final ConcertCreatorFactory concertCreatorFactory = mock(ConcertCreatorFactory.class);
    concertCreator = mock(ConcertCreator.class);
    when(concertCreatorFactory.create()).thenReturn(concertCreator);

    lifecycleNotifier = mock(LifecycleNotifier.class);
    GlobalProtocolTestFactory.builder().withLifecycleNotifier(lifecycleNotifier).create();

    actions = new ArrayList<>();

    unitUnderTest = new TheaterPlayer(concertCreatorFactory);
  }

  @Test
  public void testRegistersLifecycleListener() {
    verify(lifecycleNotifier).registerListener(unitUnderTest);
  }

  @Test
  public void testPublishesConcertOnPlay() {
    unitUnderTest.play(actions);
    verify(concertCreator).publishConcert(actions);
  }

  @Test
  public void testThrowsExceptionIfPlayCalledMoreThanOnce() {
    unitUnderTest.play(actions);
    final Exception actual =
        assertThrows(TheaterRuntimeException.class, () -> unitUnderTest.play(actions));
    assertEquals(ExceptionKeys.DUPLICATE_PLAY_COMMAND.toString(), actual.getMessage());
  }

  @Test
  public void testCleansUpConcertCreatorIfExceptionThrown() {
    doThrow(new InternalServerRuntimeError(InternalErrorKey.INTERNAL_EXCEPTION))
        .when(concertCreator)
        .publishConcert(actions);
    try {
      unitUnderTest.play(actions);
    } catch (InternalServerRuntimeError e) {
      // expected
      verify(concertCreator).close();
    }
  }

  @Test
  public void testExecutionEndedResetsHasPlayed() {
    unitUnderTest.play(actions);
    assertThrows(TheaterRuntimeException.class, () -> unitUnderTest.play(actions));

    unitUnderTest.onExecutionEnded();

    assertDoesNotThrow(() -> unitUnderTest.play(actions));
  }
}
