package org.code.protocol;

public class MetricClientManager {
  private static MetricClientManager clientManagerInstance;

  private MetricClient metricClient;

  private MetricClientManager(MetricClient metricClient) {
    this.metricClient = metricClient;
  }

  public static void create(MetricClient metricClient) {
    MetricClientManager.clientManagerInstance = new MetricClientManager(metricClient);
  }

  public static MetricClientManager getInstance() {
    if (MetricClientManager.clientManagerInstance == null) {
      throw new InternalServerRuntimeException(
          InternalExceptionKey.INTERNAL_EXCEPTION, new Exception("Metrics client not initialized"));
    }

    return MetricClientManager.clientManagerInstance;
  }

  public MetricClient getMetricClient() {
    return this.metricClient;
  }
}
