package org.code.javabuilder;

import java.time.Instant;
import org.code.protocol.MetricClient;

public class AWSMetricClient implements MetricClient {
  @Override
  public void publishSevereError() {}

  @Override
  public void publishColdBootTime(Instant coldBootTime) {}

  @Override
  public void publishInitializationTime(Instant initializationTime) {}
}
