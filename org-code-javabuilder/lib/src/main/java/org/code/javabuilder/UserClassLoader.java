package org.code.javabuilder;

import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.StringConcatFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.code.protocol.LoggerUtils;

/**
 * Custom class loader for user-provided code. This class loader only allows certain classes to be
 * used within a user-provided class.
 *
 * <p>Validation runs use a pair of these loaders (see {@link #createValidatorPair}): a USER-level
 * loader that defines only the student's classes, and a VALIDATOR-level loader that defines only
 * the validation classes and delegates student class names to the student loader. Because the JVM
 * resolves each class's symbolic references through its defining loader, the validator-only
 * allowances (org.code.validation, java.lang.reflect) are reachable from validation code but not
 * from student code.
 *
 * <p>One deliberate hole: EasyMock (cglib) defines mock subclasses inside the mocked class's own
 * loader, so mocking a student class only links if the student loader can resolve org.easymock.*
 * and its dependencies. The student half of a validator pair therefore allows the
 * mocking support lists below.
 */
public class UserClassLoader extends URLClassLoader {
  private final Set<String> userProvidedClasses;
  private final URLClassLoader approvedClassLoader;
  private final RunPermissionLevel permissionLevel;
  // Set only on the validation half of a validator pair: the loader that defines the student's
  // classes, consulted so validation code and student code share one copy of each student class.
  private final UserClassLoader studentDelegate;
  // Set only on the student half of a validator pair: allows the classes EasyMock-generated mock
  // subclasses of student classes need to link.
  private final boolean allowsMockingSupport;

  public UserClassLoader(
      URL[] urls,
      ClassLoader parent,
      List<String> userProvidedClasses,
      RunPermissionLevel permissionLevel) {
    this(urls, parent, userProvidedClasses, permissionLevel, null, false);
  }

  private UserClassLoader(
      URL[] urls,
      ClassLoader parent,
      List<String> userProvidedClasses,
      RunPermissionLevel permissionLevel,
      UserClassLoader studentDelegate,
      boolean allowsMockingSupport) {
    super(urls, parent);
    this.userProvidedClasses = new HashSet<>();
    this.userProvidedClasses.addAll(userProvidedClasses);
    // Share the student loader's approved class loader so classes loaded through it (org.code.*,
    // JUnit, EasyMock) have a single identity across the pair.
    this.approvedClassLoader =
        studentDelegate != null
            ? studentDelegate.approvedClassLoader
            : new URLClassLoader(urls, JavaRunner.class.getClassLoader());
    this.permissionLevel = permissionLevel;
    this.studentDelegate = studentDelegate;
    this.allowsMockingSupport = allowsMockingSupport;
  }

  /**
   * Creates the pair of class loaders used for a validation run: a USER-level loader defining the
   * student's classes and a VALIDATOR-level loader defining the validation classes. The validation
   * loader delegates student class names to the student loader, so student classes resolve their
   * own references at USER level even when loaded or invoked from validation code.
   */
  public static ValidatorClassLoaderPair createValidatorPair(
      URL[] urls,
      ClassLoader parent,
      List<String> studentClassNames,
      List<String> validationClassNames) {
    UserClassLoader studentLoader =
        new UserClassLoader(urls, parent, studentClassNames, RunPermissionLevel.USER, null, true);
    UserClassLoader validationLoader =
        new UserClassLoader(
            urls, parent, validationClassNames, RunPermissionLevel.VALIDATOR, studentLoader, false);
    return new ValidatorClassLoaderPair(studentLoader, validationLoader);
  }

  /** The student/validation class loader pair used for a validation run. */
  public static final class ValidatorClassLoaderPair implements Closeable {
    private final UserClassLoader studentLoader;
    private final UserClassLoader validationLoader;

    private ValidatorClassLoaderPair(
        UserClassLoader studentLoader, UserClassLoader validationLoader) {
      this.studentLoader = studentLoader;
      this.validationLoader = validationLoader;
    }

    public UserClassLoader getStudentLoader() {
      return this.studentLoader;
    }

    public UserClassLoader getValidationLoader() {
      return this.validationLoader;
    }

    // TODO: the shared approvedClassLoader is never closed (same behavior as before the pair
    // existed).
    @Override
    public void close() throws IOException {
      try {
        this.validationLoader.close();
      } finally {
        this.studentLoader.close();
      }
    }
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    // Call super for user provided classes, as we need to verify users are not
    // trying to use an unapproved class or package.
    if (this.userProvidedClasses.contains(name)) {
      return super.loadClass(name);
    }
    // Student classes are defined by the student loader, so validation code and the student's own
    // code see the same Class objects, and student classes resolve their references at USER level.
    if (this.studentDelegate != null && this.studentDelegate.isUserProvidedClass(name)) {
      return this.studentDelegate.loadClass(name);
    }
    // If this is not a user provided class, we are loading something used by a user provided class.
    // If it is either an allowed class or package, we can load with our standard class loader.
    if (this.allowedClasses.contains(name)) {
      return this.approvedClassLoader.loadClass(name);
    }

    // EasyMock support: needed by validation code, and by the student half of a validator pair
    // because mock subclasses of student classes are defined inside the student loader.
    if ((this.permissionLevel == RunPermissionLevel.VALIDATOR || this.allowsMockingSupport)
        && (this.mockingAllowedClasses.contains(name)
            || this.isInAllowedPackage(this.mockingAllowedPackages, name))) {
      return this.approvedClassLoader.loadClass(name);
    }

    // allow .<specific-class> usage from allowed packages. If this code
    // has validation permissions, also check the
    // validator permissions allowed package list.
    if (this.isInAllowedPackage(this.allowedPackages, name)
        || (this.permissionLevel == RunPermissionLevel.VALIDATOR
            && this.isInAllowedPackage(this.validatorOnlyAllowedPackages, name))) {
      return this.approvedClassLoader.loadClass(name);
    }

    // Log that we are going to throw an exception. Log as a warning
    // as it is most likely user error, but we want to track it.
    LoggerUtils.logWarning("Invalid Class", name);
    throw new ClassNotFoundException(name);
  }

  boolean isUserProvidedClass(String name) {
    return this.userProvidedClasses.contains(name);
  }

  URLClassLoader getApprovedClassLoader() {
    return this.approvedClassLoader;
  }

  /**
   * @param allowedPackageList
   * @param name
   * @return true if name is in a package a in the allowedPackageList, i.e. if name is prefixed with
   *     any value in allowedPackageList
   */
  private boolean isInAllowedPackage(String[] allowedPackageList, String name) {
    for (int i = 0; i < allowedPackageList.length; i++) {
      if (name.startsWith(allowedPackageList[i])) {
        return true;
      }
    }
    return false;
  }

  // Allowed individual classes.
  private static final Set<String> allowedClasses =
      Set.of(
          ArithmeticException.class.getName(),
          ArrayIndexOutOfBoundsException.class.getName(),
          Boolean.class.getName(),
          Byte.class.getName(),
          Character.class.getName(),
          CharSequence.class.getName(),
          Class.class.getName(),
          Comparable.class.getName(),
          Double.class.getName(),
          Enum.class.getName(),
          Exception.class.getName(),
          Float.class.getName(),
          IndexOutOfBoundsException.class.getName(),
          Integer.class.getName(),
          LambdaMetafactory.class.getName(), // needed if you want to create a lambda function
          StringConcatFactory.class.getName(), // needed for any String concatenation
          IllegalArgumentException.class.getName(),
          Long.class.getName(),
          Math.class.getName(),
          NullPointerException.class.getName(),
          Number.class.getName(),
          Object.class.getName(),
          RuntimeException.class.getName(),
          SecurityException.class.getName(),
          Short.class.getName(),
          StackTraceElement.class.getName(),
          String.class.getName(),
          StringBuffer.class.getName(),
          StringBuilder.class.getName(),
          Throwable.class.getName());

  // Allowed packages (any individual class is allowed from these classes)
  private static final String[] allowedPackages =
      new String[] {
        "java.io.",
        "java.math.",
        "java.text.",
        "java.time.",
        "java.util.",
        "org.junit.jupiter.api.",
        "org.code.media.",
        "org.code.neighborhood.",
        "org.code.theater.",
        "org.code.lang",
        "jdk.internal.reflect.SerializationConstructorAccessorImpl" // EasyMock support
      };

  // Allowed packages for code with elevated permissions, such as validation code. Never allowed
  // for student code, including the student half of a validator pair.
  private static final String[] validatorOnlyAllowedPackages =
      new String[] {"org.code.validation", "java.lang.reflect"};

  // EasyMock support: allowed for validation code and for the student half of a validator pair,
  // because EasyMock defines mock subclasses of student classes inside the student loader.
  private static final String[] mockingAllowedPackages = new String[] {"org.easymock."};

  private static final Set<String> mockingAllowedClasses =
      Set.of(
          ThreadLocal.class.getName(), // EasyMock support
          CloneNotSupportedException.class.getName(), // EasyMock support
          InvocationTargetException.class.getName(), // EasyMock support
          // EasyMock support: cglib-generated mock subclasses hold static Method fields resolved
          // during class initialization.
          Method.class.getName());
}
