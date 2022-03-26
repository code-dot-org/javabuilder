package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.time.Clock;
import java.time.Instant;
import java.util.logging.Logger;
import org.code.protocol.LoggerConstants;
import org.json.JSONObject;

public class PerformanceTracker {
  private static final String FIRST_INSTANCE = "firstInstance";
  private static final String COLD_BOOT_START = "coldBootStart";
  private static final String COLD_BOOT_END = "coldBootEnd";
  private static final String INSTANCE_START = "instanceStart";
  private static final String COMPILE_START = "compileStart";
  private static final String COMPILE_END = "compileEnd";
  private static final String USER_CODE_START = "userCodeStart";
  private static final String USER_CODE_END = "userCodeEnd";
  private static final String INSTANCE_END = "instanceEnd";
  private static final String TYPE = "performanceReport";
  private final JSONObject logs;

  public PerformanceTracker() {
    logs = new JSONObject();
  }

  public void trackStartup(
      Instant coldBootStart, Instant coldBootEnd, Instant instanceStart, boolean firstInstance) {
    logs.put(COLD_BOOT_START, coldBootStart.toString());
    logs.put(COLD_BOOT_END, coldBootEnd.toString());
    logs.put(INSTANCE_START, instanceStart.toString());
    logs.put(FIRST_INSTANCE, firstInstance);
  }

  public void trackCompileStart() {
    logs.put(COMPILE_START, Clock.systemUTC().instant().toString());
  }

  public void trackCompileEnd() {
    logs.put(COMPILE_END, Clock.systemUTC().instant().toString());
  }

  public void trackUserCodeStart() {
    logs.put(USER_CODE_START, Clock.systemUTC().instant().toString());
  }

  public void trackUserCodeEnd() {
    logs.put(USER_CODE_END, Clock.systemUTC().instant().toString());
  }

  public void trackInstanceEnd() {
    logs.put(INSTANCE_END, Clock.systemUTC().instant().toString());
  }

  public void logPerformance() {
    logs.put(LoggerConstants.TYPE, TYPE);
    Logger.getLogger(MAIN_LOGGER).info(logs.toString());
  }
}
