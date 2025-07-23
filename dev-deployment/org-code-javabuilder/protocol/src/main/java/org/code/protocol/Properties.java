package org.code.protocol;

public class Properties {
  /** The connection ID for the current session */
  private static String connectionId = "localhost";
  /** If Javabuilder can access assets from the Dashboard service that invoked it */
  private static boolean CAN_ACCESS_DASHBOARD_ASSETS = true;

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
}
