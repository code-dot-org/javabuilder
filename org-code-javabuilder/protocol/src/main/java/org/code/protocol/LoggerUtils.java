package org.code.protocol;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.util.logging.Logger;
import org.json.JSONObject;

public class LoggerUtils {
  public static void sendWarningForException(Exception e) {
    JSONObject eventData = new JSONObject();
    eventData.put(LoggerConstants.EXCEPTION_MESSAGE, e.getMessage());
    eventData.put(LoggerConstants.TYPE, e.getClass().getSimpleName());
    Logger.getLogger(MAIN_LOGGER).warning(eventData.toString());
  }
}
