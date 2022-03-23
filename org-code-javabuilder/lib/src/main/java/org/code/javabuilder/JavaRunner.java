package org.code.javabuilder;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.code.javabuilder.util.JarUtils;
import org.code.protocol.JavabuilderException;
import org.code.protocol.OutputAdapter;
import org.code.protocol.StatusMessage;
import org.code.protocol.StatusMessageKey;

/** The class that executes the student's code */
public class JavaRunner {
  private final URL executableLocation;
  private final MainRunner mainRunner;
  private final UserTestRunner userTestRunner;
  private final ValidationRunner validationRunner;
  private final List<String> javaClassNames;
  private final List<String> validationAndJavaClassNames;
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
    this.validationAndJavaClassNames = new ArrayList<>(this.javaClassNames);
    this.validationAndJavaClassNames.addAll(this.parseClassNames(validationFiles));
    this.outputAdapter = outputAdapter;
  }

  /**
   * Run the compiled user code.
   *
   * @throws InternalServerError When the user's code hits a runtime error or fails due to an
   *     internal error.
   * @throws InternalFacingException When we hit an internal error after the user's code has
   *     finished executing.
   */
  public void runMain() throws InternalFacingException, JavabuilderException {
    this.run(this.mainRunner, RunPermissionLevel.USER, this.javaClassNames);
  }

  public void runTests() throws JavabuilderException, InternalFacingException {
    // Tests have more permissions than a regular run: as of now, all
    // tests will be run under the VALIDATOR permission. Once we split out validation and
    // project tests run we will need to give different permissions to each run type.
    boolean hasValidation =
        this.run(
            this.validationRunner, RunPermissionLevel.VALIDATOR, this.validationAndJavaClassNames);
    boolean hasUserTests =
        this.run(this.userTestRunner, RunPermissionLevel.USER, this.javaClassNames);
    if (!hasValidation && !hasUserTests) {
      this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.NO_TESTS_FOUND));
    }
  }

  private boolean run(
      CodeRunner runner, RunPermissionLevel permissionLevel, List<String> classNames)
      throws JavabuilderException, InternalFacingException {
    // Include the user-facing api jars in the code we are loading so student code can access them.
    URL[] classLoaderUrls = JarUtils.getAllJarURLs(this.executableLocation);

    // Create a new UserClassLoader. This class loader handles blocking any disallowed
    // packages/classes.
    UserClassLoader urlClassLoader =
        new UserClassLoader(
            classLoaderUrls, JavaRunner.class.getClassLoader(), classNames, permissionLevel);

    boolean runResult = runner.run(urlClassLoader);

    try {
      urlClassLoader.close();
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
