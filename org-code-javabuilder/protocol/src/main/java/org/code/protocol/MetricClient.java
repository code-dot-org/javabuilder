package org.code.protocol;

// Interface for logging metrics for use in monitoring and alerting.
public interface MetricClient {
  void publishSevereError();

  void publishColdBootTime(long coldBootTime);

  void publishInitializationTime(long initializationTime);

  void publishTransitionTime(long transitionTime);

  void publishCleanupTime(long cleanupTime);
}
