package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FilePermission;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
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
  public void studentCannotAccessEnvironmentVariables() {
    assertFalse(policy.implies(userDomain, new RuntimePermission("getenv.AWS_SECRET_ACCESS_KEY")));
    assertFalse(policy.implies(userDomain, new RuntimePermission("getenv.*")));
  }

  @Test
  public void studentRetainsOtherPermissions() {
    // File and env are the only things confined; other capabilities are preserved.
    assertTrue(policy.implies(userDomain, new RuntimePermission("modifyThread")));
    assertTrue(policy.implies(userDomain, new PropertyPermission("user.language", "read")));
  }

  @Test
  public void validatorRunIsAlsoConfined() {
    // The validation run loads and executes student code in a VALIDATOR-level UserClassLoader, so
    // it must be confined the same way as a USER run.
    final String tmpFile = System.getProperty("java.io.tmpdir") + "/validation-output.txt";
    assertTrue(policy.implies(validatorDomain, new FilePermission(tmpFile, "write")));
    assertFalse(policy.implies(validatorDomain, new FilePermission("/etc/passwd", "read")));
    assertFalse(
        policy.implies(validatorDomain, new RuntimePermission("getenv.AWS_SECRET_ACCESS_KEY")));
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
