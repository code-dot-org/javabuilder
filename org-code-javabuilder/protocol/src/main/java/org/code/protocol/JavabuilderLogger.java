package org.code.protocol;

import org.json.JSONObject;

public interface JavabuilderLogger {
  public void logInfo(JSONObject eventData);

  public void logError(JSONObject eventData);
}
