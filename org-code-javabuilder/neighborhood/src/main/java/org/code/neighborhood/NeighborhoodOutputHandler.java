package org.code.neighborhood;

public class NeighborhoodOutputHandler {
  public static void sendMessage(NeighborhoodSignalMessage message) {
    // This is a hack that we are temporarily using while we design a better system to handle passing signals from Javabuilder mini apps to Java Lab
    // TODO: replace this with a more formal messaging system.
    System.out.print(message.getFormattedMessage());
  }
}
