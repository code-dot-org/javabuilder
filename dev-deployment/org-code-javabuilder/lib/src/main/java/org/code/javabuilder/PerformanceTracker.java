package org.code.javabuilder;

import java.time.Clock;
import java.time.Instant;
import org.code.protocol.*;
import org.json.JSONObject;

/**
 * This is a helper class that allows performance analysis of a variety of key events that occur
 * while a user's code is compiling and running. This is mostly intended to help analyze performance
 * metrics on our Lambdas to drive down the execution time for the portions we control, but it can
 * also be used within our local dev instances of Javabuilder, such as the WebSocketServer.
 * Throughout the execution of a student's project, events can be logged to the performance tracker.
 * Then, at the end of the project execution, PerformanceTracker.logPerformance can be invoked in
 * order to create a single record of the performance of the project execution.
 */
public class PerformanceTracker extends JavabuilderSharedObject {
  private static final String FIRST_INSTANCE = "firstInstance";
  private static final String COLD_BOOT_START = "coldBootStart";
  private static final String COLD_BOOT_END = "coldBootEnd";
  private static final String COLD_BOOT_TIME = "coldBootTime";
  private static final String INSTANCE_START = "instanceStart";
  private static final String COMPILE_START = "compileStart";;
  private static final String INITIALIZATION_TIME = "initializationTime";
  private static final String COMPILE_END = "compileEnd";
  private static final String USER_CODE_START = "userCodeStart";
  private static final String TRANSITION_TIME = "transitionTime";
  private static final String USER_CODE_END = "userCodeEnd";
  private static final String INSTANCE_END = "instanceEnd";
  private static final String CLEANUP_TIME = "cleanupTime";
  private static final String TYPE = "performanceReport";
  private final JSONObject logs;

  public PerformanceTracker() {
    this.logs = new JSONObject();
  }

  public void trackColdBoot(Instant coldBootStart, Instant coldBootEnd, Instant instanceStart) {
    logs.put(COLD_BOOT_START, coldBootStart.toEpochMilli());
    logs.put(COLD_BOOT_END, coldBootEnd.toEpochMilli());
    logs.put(INSTANCE_START, instanceStart.toEpochMilli());
    logs.put(FIRST_INSTANCE, true);
  }

  /**
   * Unlike the other tracking methods, we take an input here because we want to track the very
   * first moment when the lambda starts without waiting for other logic to run. Therefore, we take
   * a snapshot of the Instant at the very beginning of any setup logic, and then set up the
   * Performance tracker, and then we log that snapshot of the Instant.
   */
  public void trackInstanceStart(Instant instanceStart) {
    logs.put(INSTANCE_START, instanceStart.toEpochMilli());
  }

  public void trackCompileStart() {
    logs.put(COMPILE_START, Clock.systemUTC().instant().toEpochMilli());
  }

  public void trackCompileEnd() {
    logs.put(COMPILE_END, Clock.systemUTC().instant().toEpochMilli());
  }

  public void trackUserCodeStart() {
    logs.put(USER_CODE_START, Clock.systemUTC().instant().toEpochMilli());
  }

  public void trackUserCodeEnd() {
    logs.put(USER_CODE_END, Clock.systemUTC().instant().toEpochMilli());
  }

  public void trackInstanceEnd() {
    logs.put(INSTANCE_END, Clock.systemUTC().instant().toEpochMilli());
  }

  public void logPerformance() {
    logs.put(LoggerConstants.TYPE, TYPE);
    MetricClient metricClient =
        (MetricClient) JavabuilderContext.getInstance().get(MetricClient.class);
    if (!logs.isNull(COLD_BOOT_START) && !logs.isNull(COLD_BOOT_END)) {
      long coldBootTime = logs.getLong(COLD_BOOT_END) - logs.getLong(COLD_BOOT_START);
      logs.put(COLD_BOOT_TIME, coldBootTime);
      metricClient.publishColdBootTime(coldBootTime);
    }

    if (!logs.isNull(COMPILE_START) && !logs.isNull(INSTANCE_START)) {
      long initializationTime = logs.getLong(COMPILE_START) - logs.getLong(INSTANCE_START);
      logs.put(INITIALIZATION_TIME, initializationTime);
      metricClient.publishInitializationTime(initializationTime);
    }

    if (!logs.isNull(COMPILE_END) && !logs.isNull(USER_CODE_START)) {
      long transitionTime = logs.getLong(USER_CODE_START) - logs.getLong(COMPILE_END);
      logs.put(TRANSITION_TIME, transitionTime);
      metricClient.publishTransitionTime(transitionTime);
    }

    if (!logs.isNull(USER_CODE_END) && !logs.isNull(INSTANCE_END)) {
      long cleanupTime = logs.getLong(INSTANCE_END) - logs.getLong(USER_CODE_END);
      logs.put(CLEANUP_TIME, cleanupTime);
      metricClient.publishCleanupTime(cleanupTime);
    }

    LoggerUtils.logInfo(logs.toString());
  }
}
