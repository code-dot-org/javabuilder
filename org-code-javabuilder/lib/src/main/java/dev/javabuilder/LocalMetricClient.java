package dev.javabuilder;

import org.code.protocol.MetricClient;

public class LocalMetricClient implements MetricClient {
  @Override
  public void publishSevereError() {}

  @Override
  public void publishColdBootTime(long coldBootTime) {}

  @Override
  public void publishInitializationTime(long initializationTime) {}

  @Override
  public void publishTransitionTime(long transitionTime) {}

  @Override
  public void publishCleanupTime(long cleanupTime) {}
}
