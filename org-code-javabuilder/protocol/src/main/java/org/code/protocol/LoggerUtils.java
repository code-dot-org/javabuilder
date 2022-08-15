package org.code.protocol;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import org.json.JSONObject;

public class LoggerUtils {
  private static final String DISK_SPACE_LOG_TYPE = "diskSpaceUtilization";

  public enum SessionTime {
    START_SESSION("startSession"),
    END_SESSION("endSession");

    String metricName;

    SessionTime(String name) {
      this.metricName = name;
    }
  }

  public enum ClearStatus {
    BEFORE_CLEAR("beforeClear"),
    AFTER_CLEAR("afterClear");

    String metricName;

    ClearStatus(String name) {
      this.metricName = name;
    }
  }

  /**
   * Logs metrics describing the total, free, and usable disk space in the temp directory. Meant to
   * be published once per session.
   */
  public static void sendDiskSpaceReport() {
    LoggerUtils.sendDiskSpaceLogs(DISK_SPACE_LOG_TYPE);
  }

  /**
   * Logs an update describing the disk space in the temp directory. This update can be published at
   * various times per session, described by {@link SessionTime} and {@link ClearStatus}
   *
   * @param sessionTime the time in the session this update occurred
   * @param clearStatus whether this update happened before or after the directory was cleared
   */
  public static void sendDiskSpaceUpdate(SessionTime sessionTime, ClearStatus clearStatus) {
    final String type =
        DISK_SPACE_LOG_TYPE + "-" + sessionTime.metricName + "-" + clearStatus.metricName;
    LoggerUtils.sendDiskSpaceLogs(type);
  }

  public static void sendClearedDirectoryLog(Path p) {
    JSONObject eventData = new JSONObject();
    eventData.put(LoggerConstants.TYPE, "clearedDirectory");
    eventData.put(LoggerConstants.DIRECTORY, p.toFile().getPath());
    Logger.getLogger(MAIN_LOGGER).info(eventData.toString());
  }

  /** Errors logged in this way will trigger alarms that should be dealt with urgently. */
  public static void logSevereError(JavabuilderException error) {
    LoggerUtils.logSevereError(
        error.getExceptionMessage(), error.getLoggingString(), error.getCause());
  }

  /** Errors logged in this way will trigger alarms that should be dealt with urgently. */
  public static void logSevereError(
      JavabuilderThrowableMessage exceptionMessage, String loggingString, Throwable cause) {
    JSONObject eventData = new JSONObject();
    eventData.put(LoggerConstants.EXCEPTION_MESSAGE, exceptionMessage);
    eventData.put(LoggerConstants.LOGGING_STRING, loggingString);
    if (cause != null) {
      eventData.put(LoggerConstants.CAUSE, cause);
    }
    MetricClient metricClient =
        (MetricClient) JavabuilderContext.getInstance().get(MetricClient.class);
    metricClient.publishSevereError();
    Logger.getLogger(MAIN_LOGGER).severe(eventData.toString());
  }

  /** Exceptions logged in this way will trigger alarms that should be dealt with urgently. */
  public static void logSevereException(Throwable e) {
    JSONObject eventData = new JSONObject();
    eventData.put(LoggerConstants.EXCEPTION_MESSAGE, e.getMessage());
    if (e.getCause() != null) {
      eventData.put(LoggerConstants.CAUSE, e.getCause());
    }
    MetricClient metricClient =
        (MetricClient) JavabuilderContext.getInstance().get(MetricClient.class);
    metricClient.publishSevereError();
    Logger.getLogger(MAIN_LOGGER).severe(eventData.toString());
  }

  /**
   * Exceptions logged in this way are intended to be informative, but should be removed after a
   * period of time.
   */
  public static void logTrackingException(Throwable e) {
    JSONObject eventData = new JSONObject();
    eventData.put(LoggerConstants.EXCEPTION_MESSAGE, e.getMessage());
    if (e.getCause() != null) {
      eventData.put(LoggerConstants.CAUSE, e.getCause());
    }
    Logger.getLogger(MAIN_LOGGER).warning(eventData.toString());
  }

  public static void logInfo(String info) {
    Logger.getLogger(MAIN_LOGGER).info(info);
  }

  private static void sendDiskSpaceLogs(String type) {
    File f = Paths.get(System.getProperty("java.io.tmpdir")).toFile();
    JSONObject eventData = new JSONObject();
    eventData.put(LoggerConstants.TYPE, type);
    eventData.put(LoggerConstants.DIRECTORY, f.getPath());
    eventData.put(LoggerConstants.USABLE_SPACE, f.getUsableSpace());
    eventData.put(LoggerConstants.FREE_SPACE, f.getFreeSpace());
    eventData.put(LoggerConstants.TOTAL_SPACE, f.getTotalSpace());
    Logger.getLogger(MAIN_LOGGER).info(eventData.toString());
  }
}
