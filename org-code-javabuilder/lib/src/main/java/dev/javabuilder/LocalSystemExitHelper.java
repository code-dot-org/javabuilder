package dev.javabuilder;

import org.code.javabuilder.PerformanceTracker;
import org.code.javabuilder.SystemExitHelper;

public class LocalSystemExitHelper implements SystemExitHelper {

  @Override
  public void exit(int status) {
    PerformanceTracker.getInstance().trackInstanceEnd();
    PerformanceTracker.getInstance().logPerformance();
    System.exit(status);
  }
}
