package org.code.javabuilder;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.code.javabuilder.util.JarUtils;
import org.code.protocol.*;

/** The class that executes the student's code */
public class JavaRunner {
  private final URL executableLocation;
  private final MainRunner mainRunner;
  private final UserTestRunner userTestRunner;
  private final ValidationRunner validationRunner;
  private final List<String> javaClassNames;
  private final List<String> validationClassNames;
  private final OutputAdapter outputAdapter;

  public JavaRunner(
      URL executableLocation,
      List<JavaProjectFile> javaFiles,
      List<JavaProjectFile> validationFiles,
      OutputAdapter outputAdapter) {
    this(
        executableLocation,
        new MainRunner(javaFiles, outputAdapter),
        new UserTestRunner(javaFiles, outputAdapter),
        new ValidationRunner(validationFiles, javaFiles, outputAdapter),
        javaFiles,
        validationFiles,
        outputAdapter);
  }

  JavaRunner(
      URL executableLocation,
      MainRunner mainRunner,
      UserTestRunner userTestRunner,
      ValidationRunner validationRunner,
      List<JavaProjectFile> javaFiles,
      List<JavaProjectFile> validationFiles,
      OutputAdapter outputAdapter) {
    this.executableLocation = executableLocation;
    this.mainRunner = mainRunner;
    this.userTestRunner = userTestRunner;
    this.validationRunner = validationRunner;
    this.javaClassNames = this.parseClassNames(javaFiles);
    this.validationClassNames = this.parseClassNames(validationFiles);
    this.outputAdapter = outputAdapter;
  }

  /**
   * Run the compiled user code.
   *
   * @throws InternalServerException When the user's code hits a runtime error or fails due to an
   *     internal error.
   * @throws InternalFacingException When we hit an internal error after the user's code has
   *     finished executing.
   */
  public void runMain() throws InternalFacingException, JavabuilderException {
    this.run(this.mainRunner, RunPermissionLevel.USER, this.javaClassNames);
  }

  public void runTests() throws JavabuilderException, InternalFacingException {
    boolean hasValidation = this.runValidation();
    boolean hasUserTests =
        this.run(this.userTestRunner, RunPermissionLevel.USER, this.javaClassNames);
    if (!hasValidation && !hasUserTests) {
      this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.NO_TESTS_FOUND));
    }
  }

  private boolean run(
      CodeRunner runner, RunPermissionLevel permissionLevel, List<String> classNames)
      throws JavabuilderException, InternalFacingException {
    // Create a new UserClassLoader. This class loader handles blocking any disallowed
    // packages/classes.
    UserClassLoader urlClassLoader =
        new UserClassLoader(
            this.getClassLoaderUrls(),
            JavaRunner.class.getClassLoader(),
            classNames,
            permissionLevel);
    return this.execute(runner, urlClassLoader, urlClassLoader);
  }

  /**
   * Runs validation with a pair of class loaders: the validation classes load at the VALIDATOR
   * permission level while the student's classes load at the USER level, so the validator-only
   * allowances (reflection, org.code.validation) are not reachable from student code.
   */
  private boolean runValidation() throws JavabuilderException, InternalFacingException {
    UserClassLoader.ValidatorClassLoaderPair pair =
        UserClassLoader.createValidatorPair(
            this.getClassLoaderUrls(),
            JavaRunner.class.getClassLoader(),
            this.javaClassNames,
            this.validationClassNames);
    return this.execute(this.validationRunner, pair.getValidationLoader(), pair);
  }

  private URL[] getClassLoaderUrls() {
    // Include the user-facing api jars in the code we are loading so student code can access them.
    return JarUtils.getAllJarURLs(this.executableLocation);
  }

  private boolean execute(CodeRunner runner, UserClassLoader urlClassLoader, Closeable toClose)
      throws JavabuilderException, InternalFacingException {
    boolean runResult;
    PerformanceTracker performanceTracker =
        (PerformanceTracker) JavabuilderContext.getInstance().get(PerformanceTracker.class);
    performanceTracker.trackUserCodeStart();
    try {
      runResult = runner.run(urlClassLoader);
    } finally {
      performanceTracker.trackUserCodeEnd();
    }

    try {
      toClose.close();
    } catch (IOException e) {
      // The user code has finished running. We don't want to confuse them at this point with an
      // error message.
      throw new InternalFacingException("Error closing urlClassLoader: " + e, e);
    }
    return runResult;
  }

  /**
   * @param javaFiles List of java files to parse
   * @return The class names of the given java files, as a list of Strings.
   */
  private List<String> parseClassNames(List<JavaProjectFile> javaFiles) {
    return javaFiles
        .stream()
        .map(projectFile -> projectFile.getClassName())
        .collect(Collectors.toList());
  }
}
