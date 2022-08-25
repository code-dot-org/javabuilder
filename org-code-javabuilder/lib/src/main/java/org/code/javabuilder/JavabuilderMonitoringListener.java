package org.code.javabuilder;

import com.amazonaws.monitoring.ApiMonitoringEvent;
import com.amazonaws.monitoring.MonitoringEvent;
import com.amazonaws.monitoring.MonitoringListener;
import org.code.protocol.LoggerUtils;

public class JavabuilderMonitoringListener extends MonitoringListener {
  @Override
  public void handleEvent(MonitoringEvent event) {
    if (event instanceof ApiMonitoringEvent) {
      LoggerUtils.logInfo("[MONITORING EVENT] " + ((ApiMonitoringEvent) event).getApi());
    } else {
      LoggerUtils.logInfo("[MONITORING EVENT] " + event);
    }
  }
}
