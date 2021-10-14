package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.security.Permission;
import java.util.Arrays;
import java.util.logging.Logger;

public class JavabuilderSecurityManager extends SecurityManager {
  @Override
  public void checkCreateClassLoader() {
    return;
  }

  @Override
  public void checkPropertyAccess(String key) {
    return;
    //        if (key.equals("user.dir")) {
    //            return;
    //        }
    //        super.checkPropertyAccess(key);
  }

  @Override
  public void checkPackageAccess(String pkg) {
    // System.out.println("checking package access for: " + pkg);
    //        if(pkg.equals("org.code.protocol")) {
    //            throw new SecurityException("can't access org.code.protocol!");
    //        }
    //        super.checkPackageAccess(pkg);
    return;
  }

  @Override
  public void checkPermission(Permission perm) {
    String[] allowedPermNames =
        new String[] {"closeClassLoader", "accessDeclaredMembers", "control", "getClassLoader"};
    if (Arrays.asList(allowedPermNames).contains(perm.getName())) {
      return;
    }
    try {
      super.checkPermission(perm);
    } catch (SecurityException e) {
      Logger.getLogger(MAIN_LOGGER).info("would have thrown error for permission " + perm);
    }
  }
}
