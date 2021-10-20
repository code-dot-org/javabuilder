package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.security.Permission;
import java.util.Arrays;
import java.util.logging.Logger;

public class SecurityManagerLogger extends SecurityManager {

  @Override
  public void checkPermission(Permission perm) {
    String[] allowedPermNames = new String[] {"accessDeclaredMembers", "control", "getClassLoader"};
    if (Arrays.asList(allowedPermNames).contains(perm.getName())) {
      return;
    }
    try {
      super.checkPermission(perm);
    } catch (SecurityException e) {
      Logger.getLogger(MAIN_LOGGER).info("throwing error for permission " + perm);
      Logger.getLogger(MAIN_LOGGER)
          .info("class context: " + Arrays.toString(this.getClassContext()));
      throw e;
    }
  }
}
