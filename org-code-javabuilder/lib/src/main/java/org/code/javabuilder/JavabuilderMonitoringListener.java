package org.code.javabuilder;

import com.amazonaws.monitoring.MonitoringEvent;
import com.amazonaws.monitoring.MonitoringListener;
import org.code.protocol.LoggerUtils;

public class JavabuilderMonitoringListener extends MonitoringListener {
  @Override
  public void handleEvent(MonitoringEvent event) {
    LoggerUtils.logInfo("[MONITORING EVENT] " + event.toString());
  }
}
