package dev.javabuilder;

import org.code.protocol.SystemExitHelper;

public class LocalSystemExitHelper implements SystemExitHelper {

  @Override
  public void exit(int status) {
    // Currently nothing to clean up; no-op
  }
}
