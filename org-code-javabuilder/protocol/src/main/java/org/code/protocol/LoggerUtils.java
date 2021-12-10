package org.code.protocol;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class LoggerUtils {
  public static void sendWarningForException(Exception e) {
    JSONObject eventData = new JSONObject();
    eventData.put(LoggerConstants.EXCEPTION_MESSAGE, e.getMessage());
    eventData.put(LoggerConstants.TYPE, e.getClass().getSimpleName());
    Logger.getLogger(MAIN_LOGGER).warning(eventData.toString());
  }

  public static void sendFileNameList() {
    File f = Paths.get(System.getProperty("java.io.tmpdir")).toFile();
    JSONObject eventData = new JSONObject();
    JSONArray array = new JSONArray();
    String[] pathnames = f.list();
    array.putAll(pathnames);
    eventData.put(LoggerConstants.TYPE, "filesInTmp");
    eventData.put("fileNames", array);
    Logger.getLogger(MAIN_LOGGER).info(eventData.toString());
  }

  public static void sendDiskSpaceLogs() {
    File f = Paths.get(System.getProperty("java.io.tmpdir")).toFile();
    JSONObject eventData = new JSONObject();
    eventData.put(LoggerConstants.TYPE, "diskSpaceUtilization");
    eventData.put(LoggerConstants.DIRECTORY, f.getPath());
    eventData.put(LoggerConstants.USABLE_SPACE, f.getUsableSpace());
    eventData.put(LoggerConstants.FREE_SPACE, f.getFreeSpace());
    eventData.put(LoggerConstants.TOTAL_SPACE, f.getTotalSpace());
    Logger.getLogger(MAIN_LOGGER).info(eventData.toString());
    sendFileNameList();
  }

  public static void sendClearedDirectoryLog(Path p) {
    JSONObject eventData = new JSONObject();
    eventData.put(LoggerConstants.TYPE, "clearedDirectory");
    eventData.put(LoggerConstants.DIRECTORY, p.toFile().getPath());
    Logger.getLogger(MAIN_LOGGER).info(eventData.toString());
  }
}
