package org.code.javabuilder.util;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;
import org.code.protocol.LoggerUtils;

public class ProfilingUtils {
  private ProfilingUtils() {
    throw new UnsupportedOperationException("Instantiation of utility class is not allowed.");
  }

  /*
  Log current and peak memory usage across all memory pools. Also log currently loaded and unload class counts.
   */
  public static void logMemoryUsage() {
    List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
    for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
      String usageMessage =
          String.format(
              "Memory pool %s\n\t Usage: %d\n\tPeak Usage %d",
              memoryPoolMXBean.getName(),
              memoryPoolMXBean.getUsage().getUsed(),
              memoryPoolMXBean.getPeakUsage().getUsed());
      LoggerUtils.logInfo(usageMessage);
    }
    ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
    String classLoadingMessage =
        String.format(
            "Loaded classes: %d, Unloaded classes: %d",
            classLoadingMXBean.getLoadedClassCount(), classLoadingMXBean.getUnloadedClassCount());
    LoggerUtils.logInfo(classLoadingMessage);
  }
}
