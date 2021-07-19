package org.code.javabuilder;

import org.code.protocol.JavabuilderLogger;
import org.json.JSONObject;

public class TestLogger implements JavabuilderLogger {
  @Override
  public void logInfo(JSONObject eventData) {
    System.out.println("INFO: " + eventData.toString());
  }

  @Override
  public void logError(JSONObject eventData) {
    System.out.println("ERROR: " + eventData.toString());
  }
}
