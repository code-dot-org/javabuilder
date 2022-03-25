package org.code.protocol;

public class Properties {
  /** The connection ID for the current session */
  private static String connectionId = "localhost";
  /** If Javabuilder can access assets from the Dashboard service that invoked it */
  private static boolean CAN_ACCESS_DASHBOARD_ASSETS = true;
  /** Whether Javabuilder is running an integration test */
  private static boolean IS_INTEGRATION_TEST = false;

  public static void setConnectionId(String connectionId) {
    Properties.connectionId = connectionId;
  }

  public static String getConnectionId() {
    return Properties.connectionId;
  }

  public static void setCanAccessDashboardAssets(boolean canAccessDashboardAssets) {
    Properties.CAN_ACCESS_DASHBOARD_ASSETS = canAccessDashboardAssets;
  }

  public static boolean canAccessDashboardAssets() {
    return Properties.CAN_ACCESS_DASHBOARD_ASSETS;
  }

  public static void setIsIntegrationTest(boolean isIntegrationTest) {
    Properties.IS_INTEGRATION_TEST = isIntegrationTest;
  }

  public static boolean isIntegrationTest() {
    return Properties.IS_INTEGRATION_TEST;
  }
}
