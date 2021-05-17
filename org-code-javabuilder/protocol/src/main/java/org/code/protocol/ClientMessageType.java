package org.code.protocol;

/** Message keys that map 1:1 to client-side usages. */
public enum ClientMessageType {
  /** A message directed to the client-side terminal. Equivalent to System.out.print. */
  SYSTEM_OUT,
  /** An exception that should be displayed to the user. */
  EXCEPTION,
  /** A neighborhood signal that directs the client-side Neighborhood program to take an action. */
  NEIGHBORHOOD,
  /** An message directed to the client in local development mode */
  DEBUG
}
