package dev.javabuilder;

import org.code.javabuilder.PerformanceTracker;
import org.code.javabuilder.SystemExitHelper;
import org.code.protocol.JavabuilderContext;

public class LocalSystemExitHelper implements SystemExitHelper {

  @Override
  public void exit(int status) {
    PerformanceTracker performanceTracker =
        (PerformanceTracker) JavabuilderContext.getInstance().get(PerformanceTracker.class);
    performanceTracker.trackInstanceEnd();
    performanceTracker.logPerformance();
    System.exit(status);
  }
}
