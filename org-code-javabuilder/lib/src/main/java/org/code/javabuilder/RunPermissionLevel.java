package org.code.javabuilder;

/** Permission level for running code. */
public enum RunPermissionLevel {
  // Basic permission level. Has access to all standard packages.
  USER,
  // Permission level for running validation code. Has access to everything user has access to,
  // plus validation-specific packages.
  VALIDATOR
}
