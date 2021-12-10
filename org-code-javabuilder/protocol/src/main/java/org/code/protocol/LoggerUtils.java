package org.code.protocol;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import org.json.JSONObject;

public class LoggerUtils {
  public static void sendDiskSpaceLogs() {
    File f = Paths.get(System.getProperty("java.io.tmpdir")).toFile();
    JSONObject eventData = new JSONObject();
    eventData.put(LoggerConstants.TYPE, "diskSpaceUtilization");
    eventData.put(LoggerConstants.DIRECTORY, f.getPath());
    eventData.put(LoggerConstants.USABLE_SPACE, f.getUsableSpace());
    eventData.put(LoggerConstants.FREE_SPACE, f.getFreeSpace());
    eventData.put(LoggerConstants.TOTAL_SPACE, f.getTotalSpace());
    Logger.getLogger(MAIN_LOGGER).info(eventData.toString());
  }

  public static void sendClearedDirectoryLog(Path p) {
    JSONObject eventData = new JSONObject();
    eventData.put(LoggerConstants.TYPE, "clearedDirectory");
    eventData.put(LoggerConstants.DIRECTORY, p.toFile().getPath());
    Logger.getLogger(MAIN_LOGGER).info(eventData.toString());
  }
}
