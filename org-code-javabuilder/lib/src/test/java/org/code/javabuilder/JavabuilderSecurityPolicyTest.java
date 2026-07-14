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
    // Filesystem access is the only thing confined; other capabilities are preserved. getenv is
    // intentionally NOT denied because the AWS SDK resolves credentials via getenv while student
    // code is on the call stack (e.g. flushing a println through the output adapter).
    assertTrue(policy.implies(userDomain, new RuntimePermission("modifyThread")));
    assertTrue(policy.implies(userDomain, new PropertyPermission("user.language", "read")));
    assertTrue(policy.implies(userDomain, new RuntimePermission("getenv.AWS_SECRET_ACCESS_KEY")));
  }

  @Test
  public void studentCannotDisableTheSandbox() {
    // Confined code must never be allowed to install a new SecurityManager or Policy, which would
    // turn off all confinement. This holds in both USER and VALIDATOR (reflection-capable) runs.
    for (ProtectionDomain studentDomain : new ProtectionDomain[] {userDomain, validatorDomain}) {
      assertFalse(
          policy.implies(studentDomain, new RuntimePermission("setSecurityManager")));
      assertFalse(
          policy.implies(studentDomain, new RuntimePermission("createSecurityManager")));
      assertFalse(policy.implies(studentDomain, new SecurityPermission("setPolicy")));
      assertFalse(policy.implies(studentDomain, new SecurityPermission("createPolicy.JavaPolicy")));
      assertFalse(
          policy.implies(studentDomain, new SecurityPermission("setProperty.policy.provider")));
    }
    // Framework code is unaffected: it retains full control of the security infrastructure.
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
  public void warmUpDoesNotThrowAndPolicyStillEnforces() {
    final JavabuilderSecurityPolicy freshPolicy = new JavabuilderSecurityPolicy();
    assertDoesNotThrow(freshPolicy::warmUp);
    // Warming up must not weaken enforcement.
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
