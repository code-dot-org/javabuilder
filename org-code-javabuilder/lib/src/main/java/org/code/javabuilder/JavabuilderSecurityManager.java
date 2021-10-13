package org.code.javabuilder;

import java.security.Permission;

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
    System.out.println("checking package access for: " + pkg);
    //        if(pkg.equals("org.code.protocol")) {
    //            throw new SecurityException("can't access org.code.protocol!");
    //        }
    //        super.checkPackageAccess(pkg);
    return;
  }

  @Override
  public void checkPermission(Permission perm) {
    // System.out.println("checking permission: " + perm.getName());
    //        String[] allowedPermNames = new String[]{"closeClassLoader", "accessDeclaredMembers",
    // "suppressAccessChecks", "setIO"};
    //        if (Arrays.asList(allowedPermNames).contains(perm.getName())) {
    //            return;
    //        }
    try {
      super.checkPermission(perm);
    } catch (SecurityException e) {
      System.out.println("security exception would have been thrown for " + perm);
    }
  }
}
