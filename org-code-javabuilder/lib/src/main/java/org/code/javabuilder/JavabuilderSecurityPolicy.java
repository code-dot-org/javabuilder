package org.code.javabuilder;

import java.io.File;
import java.io.FilePermission;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permission;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.SecurityPermission;
import java.security.cert.Certificate;
import java.util.List;

/**
 * Security policy that confines student code at runtime while leaving all framework, JDK, and AWS
 * SDK code fully trusted. Student code is identified by being loaded from a {@link
 * UserClassLoader}. This covers both standard USER runs and the VALIDATOR-level validation run,
 * which executes student code (the student's main method) within the same class loader.
 *
 * <p>For confined student code this policy:
 *
 * <ul>
 *   <li>allows read/write/delete only under the writable temp directory,
 *   <li>allows read-only access to the app/runtime directories the JVM and student libraries need
 *       (so class loading, fonts, and bundled assets keep working),
 *   <li>never allows execute
 *   <li>denies all other filesystem access
 *   <li>denies reading environment variables (on Lambda they include the function's AWS
 *       credentials); trusted code that calls the AWS SDK while student code is on the stack must
 *       wrap the SDK call in {@code AccessController.doPrivileged} (see AWSOutputAdapter,
 *       AWSInputAdapter, AWSContentManager),
 *   <li>denies replacing or removing the SecurityManager and this policy.
 * </ul>
 *
 * <p>NOTE: {@link SecurityManager} / {@link Policy} are deprecated in Java 17 and removed in Java
 * 21. This control is valid on the current java11 runtime only.
 */
public class JavabuilderSecurityPolicy extends Policy {
  private static final String ALL_FILES = "<<ALL FILES>>";

  // The only root confined student code may write to / delete within.
  private final String writableRoot;
  // Roots confined student code may read from (a superset of writableRoot). Everything the JVM
  // needs to load classes, fonts, and library assets lives under these
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
    // Warm up here, at construction, so it is guaranteed to run before the caller installs a
    // SecurityManager.
    warmUp();
  }

  @Override
  public boolean implies(ProtectionDomain domain, Permission permission) {
    if (!isConfinedStudentCode(domain)) {
      // Framework / JDK / AWS SDK code: full trust.
      return true;
    }
    if (permission instanceof FilePermission) {
      return isAllowedFileAccess((FilePermission) permission);
    }

    if (permission instanceof RuntimePermission) {
      final String name = permission.getName();
      if ("setSecurityManager".equals(name)) {
        return false;
      }
      // Covers both System.getenv(name) ("getenv.<name>") and System.getenv() ("getenv.*").
      if (name.startsWith("getenv.")) {
        return false;
      }
    }
    if (permission instanceof SecurityPermission && "setPolicy".equals(permission.getName())) {
      return false;
    }
    return true;
  }

  /**
   * Forces every class that #implies references to load. Called from the constructor so it always
   * runs before a SecurityManager is installed.
   *
   * <p>#implies runs on every permission check, including the File.exists() calls the class loader
   * makes to locate a class. If a class #implies references is still unloaded when the first such
   * check fires, loading it re-enters #implies before the load completes and throws
   * ClassCircularityError. Exercising the policy here, while no SecurityManager is active,
   * guarantees those classes are already loaded by the time checks begin.
   */
  private void warmUp() {
    final ProtectionDomain studentDomain =
        new ProtectionDomain(
            new CodeSource(null, (Certificate[]) null),
            null,
            new UserClassLoader(
                new URL[] {},
                ClassLoader.getSystemClassLoader(),
                List.of(),
                RunPermissionLevel.USER),
            null);
    this.implies(studentDomain, new FilePermission("warmup", "read"));
    this.implies(studentDomain, new FilePermission("warmup", "write"));
    this.implies(studentDomain, new RuntimePermission("setSecurityManager"));
    this.implies(studentDomain, new RuntimePermission("getenv.warmup"));
    this.implies(studentDomain, new SecurityPermission("setPolicy"));
  }

  private boolean isConfinedStudentCode(ProtectionDomain domain) {
    // Confine any code loaded by a UserClassLoader. This covers both standard USER runs and the
    // VALIDATOR-level validation run: validation loads and invokes the student's code (via the
    // student's main method) in the same class loader, so this ensures student code is confined
    // there too, not just in RUN and user-test modes.
    return domain != null && domain.getClassLoader() instanceof UserClassLoader;
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
    if (actions.contains("execute")) {
      return false;
    }
    if (actions.contains("write") || actions.contains("delete")) {
      return isUnder(path, this.writableRoot);
    }
    // read / readlink
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
