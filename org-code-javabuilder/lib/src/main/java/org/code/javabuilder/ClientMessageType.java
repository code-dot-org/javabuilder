package org.code.javabuilder;

/** Message keys that map 1:1 to client-side usages. */
public enum ClientMessageType {
  /** A message directed to the client-side terminal. Equivalent to System.out.print. */
  systemOut,
  /** An exception that should be displayed to the user. */
  exception,
  /** A neighborhood signal that directs the client-side Neighborhood program to take an action. */
  neighborhood
}
