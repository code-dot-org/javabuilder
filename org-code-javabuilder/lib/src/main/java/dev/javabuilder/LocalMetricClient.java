package dev.javabuilder;

import org.code.protocol.JavabuilderSharedObject;
import org.code.protocol.MetricClient;

// Local implementation of MetricClient. Since all we can do locally
// is log and we already have regular logging for every metric,
// these methods do nothing.
public class LocalMetricClient extends JavabuilderSharedObject implements MetricClient {
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
