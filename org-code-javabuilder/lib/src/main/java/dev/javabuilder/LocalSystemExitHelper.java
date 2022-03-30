package dev.javabuilder;

import org.code.javabuilder.SystemExitHelper;

public class LocalSystemExitHelper implements SystemExitHelper {

  @Override
  public void exit(int status) {
    // Currently nothing to clean up; no-op
  }
}
