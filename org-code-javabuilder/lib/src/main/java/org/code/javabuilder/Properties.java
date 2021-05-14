package org.code.javabuilder;

public class Properties {
  private static String connectionId = "localhost";

  public static void setConnectionId(String connectionId) {
    Properties.connectionId = connectionId;
  }

  public static String getConnectionId() {
    return Properties.connectionId;
  }
}
