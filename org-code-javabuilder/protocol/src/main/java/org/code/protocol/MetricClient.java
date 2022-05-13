package org.code.protocol;

public interface MetricClient {
  void publishSevereError();

  void publishColdBootTime(long coldBootTime);

  void publishInitializationTime(long initializationTime);

  void publishTransitionTime(long transitionTime);

  void publishCleanupTime(long cleanupTime);
}
