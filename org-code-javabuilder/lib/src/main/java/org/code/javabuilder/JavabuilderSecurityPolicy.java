package org.code.javabuilder;

import java.io.File;
import java.io.FilePermission;
import java.security.Permission;
import java.security.Policy;
import java.security.ProtectionDomain;

/**
 * Security policy that confines USER-level student code at runtime while leaving all framework,
 * JDK, AWS SDK, and VALIDATOR-level code fully trusted. Student code is identified by being loaded
 * from a USER-level {@link UserClassLoader}.
 *
 * <p>For confined student code this policy:
 *
 * <ul>
 *   <li>allows read/write/delete only under the writable temp directory,
 *   <li>allows read-only access to the app/runtime directories the JVM and student libraries need
 *       (so class loading, fonts, and bundled assets keep working),
 *   <li>denies all other filesystem access (notably /proc, /etc, $HOME, and env-backed files),
 *   <li>denies environment variable access (getenv), which on Lambda exposes the execution role's
 *       AWS credentials,
 *   <li>preserves every other capability student code has today.
 * </ul>
 *
 * <p>NOTE: {@link SecurityManager} / {@link Policy} are deprecated in Java 17 and removed in Java
 * 21. This control is valid on the current java11 runtime only.
 */
public class JavabuilderSecurityPolicy extends Policy {
  private static final String ALL_FILES = "<<ALL FILES>>";
  private static final String GETENV_PREFIX = "getenv.";

  // The only root confined student code may write to / delete within.
  private final String writableRoot;
  // Roots confined student code may read from (a superset of writableRoot). Everything the JVM
  // needs to load classes, fonts, and library assets lives under these; sensitive locations
  // (/proc, /etc, $HOME, env) are intentionally excluded.
  private final String[] readableRoots;

  public JavabuilderSecurityPolicy() {
    this.writableRoot = canonicalize(System.getProperty("java.io.tmpdir"));
    this.readableRoots =
        new String[] {
          this.writableRoot,
          canonicalize(System.getProperty("java.home")), // JRE: class/resource loading
          "/var/task", // deployed application code + dependency jars
          "/var/lang", // managed runtime
          "/opt" // Lambda layers: fonts, instrument samples, wrapper
        };
  }

  @Override
  public boolean implies(ProtectionDomain domain, Permission permission) {
    if (!isConfinedStudentCode(domain)) {
      // Framework / JDK / AWS SDK / validator code: full trust.
      return true;
    }
    if (permission instanceof FilePermission) {
      return isAllowedFileAccess((FilePermission) permission);
    }
    // Deny environment variable access; on Lambda this exposes AWS credentials.
    if (permission instanceof RuntimePermission
        && permission.getName() != null
        && permission.getName().startsWith(GETENV_PREFIX)) {
      return false;
    }
    // File and env access are the only things we confine here. Everything else student code can do
    // today is preserved.
    return true;
  }

  private boolean isConfinedStudentCode(ProtectionDomain domain) {
    if (domain == null || !(domain.getClassLoader() instanceof UserClassLoader)) {
      return false;
    }
    // Validation code is trusted first-party code; only confine USER-level runs.
    return ((UserClassLoader) domain.getClassLoader()).getPermissionLevel()
        == RunPermissionLevel.USER;
  }

  private boolean isAllowedFileAccess(FilePermission permission) {
    if (ALL_FILES.equals(permission.getName())) {
      return false;
    }
    final String path = canonicalize(permission.getName());
    if (path == null) {
      return false;
    }
    final String actions = permission.getActions();
    if (actions.contains("write") || actions.contains("delete")) {
      return isUnder(path, this.writableRoot);
    }
    // read / execute / readlink
    for (String root : this.readableRoots) {
      if (root != null && isUnder(path, root)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isUnder(String path, String root) {
    return path.equals(root) || path.startsWith(root + File.separator);
  }

  private static String canonicalize(String path) {
    if (path == null) {
      return null;
    }
    try {
      // getCanonicalPath() does not itself trigger a SecurityManager check, so this does not
      // recurse; resolving symlinks prevents a /tmp/link -> /etc escape.
      return new File(path).getCanonicalPath();
    } catch (Exception e) {
      return null;
    }
  }
}
