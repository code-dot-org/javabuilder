package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FilePermission;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.SecurityPermission;
import java.security.cert.Certificate;
import java.util.List;
import java.util.PropertyPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JavabuilderSecurityPolicyTest {
  private JavabuilderSecurityPolicy policy;
  private ProtectionDomain userDomain;
  private ProtectionDomain validatorDomain;
  private ProtectionDomain frameworkDomain;

  @BeforeEach
  public void setUp() {
    policy = new JavabuilderSecurityPolicy();
    userDomain = domainFor(userClassLoader(RunPermissionLevel.USER));
    validatorDomain = domainFor(userClassLoader(RunPermissionLevel.VALIDATOR));
    frameworkDomain = domainFor(JavabuilderSecurityPolicyTest.class.getClassLoader());
  }

  @Test
  public void studentCanReadAndWriteUnderTmp() {
    final String tmpFile = System.getProperty("java.io.tmpdir") + "/student-output.txt";
    assertTrue(policy.implies(userDomain, new FilePermission(tmpFile, "read")));
    assertTrue(policy.implies(userDomain, new FilePermission(tmpFile, "write")));
    assertTrue(policy.implies(userDomain, new FilePermission(tmpFile, "delete")));
  }

  @Test
  public void studentCannotReadOutsideAllowedRoots() {
    assertFalse(policy.implies(userDomain, new FilePermission("/etc/passwd", "read")));
    assertFalse(policy.implies(userDomain, new FilePermission("/proc/self/environ", "read")));
    assertFalse(policy.implies(userDomain, new FilePermission("<<ALL FILES>>", "read")));
  }

  @Test
  public void studentCannotWriteOutsideTmp() {
    // java.home is readable but not writable for student code.
    final String runtimeFile = System.getProperty("java.home") + "/lib/evil.jar";
    assertTrue(policy.implies(userDomain, new FilePermission(runtimeFile, "read")));
    assertFalse(policy.implies(userDomain, new FilePermission(runtimeFile, "write")));
  }

  @Test
  public void studentCanReadRuntimeDirectoriesForClassLoading() {
    final String runtimeFile = System.getProperty("java.home") + "/lib/modules";
    assertTrue(policy.implies(userDomain, new FilePermission(runtimeFile, "read")));
  }

  @Test
  public void studentRetainsNonFilePermissions() {
    assertTrue(policy.implies(userDomain, new RuntimePermission("modifyThread")));
    assertTrue(policy.implies(userDomain, new PropertyPermission("user.language", "read")));
  }

  @Test
  public void studentCannotReadEnvironmentVariables() {
    // The Lambda environment includes the function's AWS credentials. Trusted code that calls the
    // AWS SDK on the student's stack (output/input adapters, content manager) wraps those calls in
    // doPrivileged so this denial only affects direct student reads.
    assertFalse(policy.implies(userDomain, new RuntimePermission("getenv.AWS_SECRET_ACCESS_KEY")));
    // System.getenv() with no arguments checks "getenv.*".
    assertFalse(policy.implies(userDomain, new RuntimePermission("getenv.*")));
    assertFalse(policy.implies(validatorDomain, new RuntimePermission("getenv.*")));
  }

  @Test
  public void studentCannotExecuteEvenUnderTmp() {
    final String tmpBinary = System.getProperty("java.io.tmpdir") + "/x";
    assertFalse(policy.implies(userDomain, new FilePermission(tmpBinary, "execute")));
    assertFalse(policy.implies(userDomain, new FilePermission(tmpBinary, "read,execute")));
    // Execute is denied everywhere, including the otherwise-readable runtime roots.
    final String runtimeBinary = System.getProperty("java.home") + "/bin/java";
    assertFalse(policy.implies(userDomain, new FilePermission(runtimeBinary, "execute")));
    assertFalse(policy.implies(validatorDomain, new FilePermission(tmpBinary, "execute")));
  }

  @Test
  public void studentCannotDisableTheSandbox() {
    // System.setSecurityManager(null) would remove the sandbox; Policy.setPolicy() would let
    // student code replace this policy with an allow-everything one.
    assertFalse(policy.implies(userDomain, new RuntimePermission("setSecurityManager")));
    assertFalse(policy.implies(userDomain, new SecurityPermission("setPolicy")));
    assertFalse(policy.implies(validatorDomain, new RuntimePermission("setSecurityManager")));
    assertFalse(policy.implies(validatorDomain, new SecurityPermission("setPolicy")));
    // Framework code (LambdaRequestHandler) installs the manager and policy itself.
    assertTrue(policy.implies(frameworkDomain, new RuntimePermission("setSecurityManager")));
    assertTrue(policy.implies(frameworkDomain, new SecurityPermission("setPolicy")));
  }

  @Test
  public void validatorRunIsAlsoConfined() {
    // The validation run loads and executes student code in a VALIDATOR-level UserClassLoader, so
    // it must be confined the same way as a USER run.
    final String tmpFile = System.getProperty("java.io.tmpdir") + "/validation-output.txt";
    assertTrue(policy.implies(validatorDomain, new FilePermission(tmpFile, "write")));
    assertFalse(policy.implies(validatorDomain, new FilePermission("/etc/passwd", "read")));
  }

  @Test
  public void constructorWarmUpDoesNotThrowAndPolicyStillEnforces() {
    // Construction warms the policy up; it must not throw and must not weaken enforcement.
    final JavabuilderSecurityPolicy freshPolicy =
        assertDoesNotThrow(JavabuilderSecurityPolicy::new);
    assertFalse(freshPolicy.implies(userDomain, new FilePermission("/etc/passwd", "read")));
  }

  @Test
  public void frameworkCodeIsFullyTrusted() {
    assertTrue(policy.implies(frameworkDomain, new FilePermission("/etc/passwd", "read,write")));
    assertTrue(
        policy.implies(frameworkDomain, new RuntimePermission("getenv.AWS_SECRET_ACCESS_KEY")));
  }

  private UserClassLoader userClassLoader(RunPermissionLevel level) {
    return new UserClassLoader(
        new URL[] {}, JavabuilderSecurityPolicyTest.class.getClassLoader(), List.of(), level);
  }

  private ProtectionDomain domainFor(ClassLoader classLoader) {
    return new ProtectionDomain(
        new CodeSource(null, (Certificate[]) null), null, classLoader, null);
  }
}
