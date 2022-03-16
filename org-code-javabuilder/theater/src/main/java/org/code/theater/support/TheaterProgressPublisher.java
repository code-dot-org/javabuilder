package org.code.theater.support;

import java.util.HashMap;
import org.code.protocol.*;

/**
 * Publishes progress updates to the {@link OutputAdapter} while Theater projects are being
 * generated
 */
public class TheaterProgressPublisher {
  private static final double UPDATE_TIME_S = 5.0;
  private static final double MAX_TIME_S = 120.0;

  private final OutputAdapter outputAdapter;
  private double pauseTimeSeconds;
  private double lastUpdateTimeSeconds;

  public TheaterProgressPublisher() {
    this(GlobalProtocol.getInstance().getOutputAdapter());
  }

  TheaterProgressPublisher(OutputAdapter outputAdapter) {
    this.outputAdapter = outputAdapter;
    this.pauseTimeSeconds = 0.0;
    this.lastUpdateTimeSeconds = 0.0;
  }

  public void onPause(double seconds) {
    this.pauseTimeSeconds += seconds;
    if (this.pauseTimeSeconds > MAX_TIME_S) {
      throw new TheaterRuntimeException(ExceptionKeys.VIDEO_TOO_LONG);
    }

    if (this.pauseTimeSeconds - this.lastUpdateTimeSeconds >= UPDATE_TIME_S) {
      this.publishProgress();
      this.lastUpdateTimeSeconds = this.pauseTimeSeconds;
    }
  }

  public void onPlay(double totalAudioTimeSeconds) {
    this.publishTotal(Math.max(totalAudioTimeSeconds, this.pauseTimeSeconds));
  }

  private void publishProgress() {
    final HashMap<String, String> detail = new HashMap<>();
    detail.put(ClientMessageDetailKeys.PROGRESS_TIME, this.formatTime(this.pauseTimeSeconds));
    this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.GENERATING_PROGRESS, detail));
  }

  private void publishTotal(double totalTime) {
    final HashMap<String, String> detail = new HashMap<>();
    detail.put(ClientMessageDetailKeys.TOTAL_TIME, this.formatTime(totalTime));
    this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.SENDING_VIDEO, detail));
  }

  private String formatTime(double seconds) {
    return Integer.toString((int) Math.round(seconds));
  }
}
