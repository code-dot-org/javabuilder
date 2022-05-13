package org.code.javabuilder;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.*;
import org.code.protocol.MetricClient;

public class AWSMetricClient implements MetricClient {
  private final AmazonCloudWatch cloudWatchClient;
  private final Dimension functionNameDimension;

  private static final String NAMESPACE = "Javabuilder";

  public AWSMetricClient(String functionName) {
    this.cloudWatchClient = AmazonCloudWatchClientBuilder.defaultClient();
    this.functionNameDimension = new Dimension().withName("functionName").withValue(functionName);
  }

  @Override
  public void publishSevereError() {
    MetricDatum metricDatum =
        new MetricDatum()
            .withMetricName("SevereError")
            .withUnit(StandardUnit.Count)
            .withValue(1.0)
            .withDimensions(this.functionNameDimension);
    PutMetricDataRequest request =
        new PutMetricDataRequest().withNamespace(NAMESPACE).withMetricData(metricDatum);
    this.cloudWatchClient.putMetricData(request);
  }

  @Override
  public void publishColdBootTime(long coldBootTime) {
    this.publishMillisecondMetric("ColdBootTime", (double) coldBootTime);
  }

  @Override
  public void publishInitializationTime(long initializationTime) {
    this.publishMillisecondMetric("InitializationTime", (double) initializationTime);
  }

  @Override
  public void publishTransitionTime(long transitionTime) {
    this.publishMillisecondMetric("TransitionTime", (double) transitionTime);
  }

  @Override
  public void publishCleanupTime(long cleanupTime) {
    this.publishMillisecondMetric("CleanupTime", (double) cleanupTime);
  }

  private void publishMillisecondMetric(String metricName, double milliseconds) {
    MetricDatum metricDatum =
        new MetricDatum()
            .withMetricName(metricName)
            .withUnit(StandardUnit.Milliseconds)
            .withValue(milliseconds)
            .withDimensions(this.functionNameDimension);
    PutMetricDataRequest request =
        new PutMetricDataRequest().withNamespace(NAMESPACE).withMetricData(metricDatum);
    this.cloudWatchClient.putMetricData(request);
  }
}
