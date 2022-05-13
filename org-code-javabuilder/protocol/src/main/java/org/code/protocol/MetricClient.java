package org.code.protocol;

import java.time.Instant;

public interface MetricClient {
  void publishSevereError();

  void publishColdBootTime(Instant coldBootTime);

  void publishInitializationTime(Instant initializationTime);
}
