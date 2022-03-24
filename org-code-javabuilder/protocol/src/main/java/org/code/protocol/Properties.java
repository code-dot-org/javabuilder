package org.code.protocol;

public class Properties {
  /** The connection ID for the current session */
  private static String connectionId = "localhost";
  /** If the client invoking Javabuilder is the Dashboard service running on localhost */
  private static boolean DASHBOARD_LOCALHOST = false;
  /** Whether Javabuilder is running an integration test */
  private static boolean IS_INTEGRATION_TEST = false;

  public static void setConnectionId(String connectionId) {
    Properties.connectionId = connectionId;
  }

  public static String getConnectionId() {
    return Properties.connectionId;
  }

  public static void setIsDashboardLocalhost(boolean isDashboardLocalhost) {
    Properties.DASHBOARD_LOCALHOST = isDashboardLocalhost;
  }

  public static boolean isDashboardLocalhost() {
    return Properties.DASHBOARD_LOCALHOST;
  }

  public static void setIsIntegrationTest(boolean isIntegrationTest) {
    Properties.IS_INTEGRATION_TEST = isIntegrationTest;
  }

  public static boolean isIntegrationTest() {
    return Properties.IS_INTEGRATION_TEST;
  }
}
