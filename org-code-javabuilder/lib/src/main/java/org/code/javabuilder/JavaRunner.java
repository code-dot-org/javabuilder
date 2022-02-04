package org.code.javabuilder;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.code.protocol.JavabuilderException;
import org.code.protocol.OutputAdapter;

/** The class that executes the student's code */
public class JavaRunner {
  private final URL executableLocation;
  private final MainRunner mainRunner;
  private final TestRunner testRunner;
  private final List<String> javaClassNames;

  public JavaRunner(
      URL executableLocation, List<JavaProjectFile> javaFiles, OutputAdapter outputAdapter) {
    this(
        executableLocation,
        new MainRunner(javaFiles, outputAdapter),
        new TestRunner(javaFiles, outputAdapter),
        javaFiles);
  }

  JavaRunner(
      URL executableLocation,
      MainRunner mainRunner,
      TestRunner testRunner,
      List<JavaProjectFile> javaFiles) {
    this.executableLocation = executableLocation;
    this.mainRunner = mainRunner;
    this.testRunner = testRunner;
    this.javaClassNames = this.parseClassNames(javaFiles);
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
    this.run(this.mainRunner, RunPermissionLevel.USER);
  }

  public void runTests() throws JavabuilderException, InternalFacingException {
    // Tests have more permissions than a regular run: as of now, all
    // tests will be run under the VALIDATOR permission. Once we split out validation and
    // project tests run we will need to give different permissions to each run type.
    this.run(this.testRunner, RunPermissionLevel.VALIDATOR);
  }

  private void run(CodeRunner runner, RunPermissionLevel permissionLevel)
      throws JavabuilderException, InternalFacingException {
    // Include the user-facing api jars in the code we are loading so student code can access them.
    URL[] classLoaderUrls = Util.getAllJarURLs(this.executableLocation);

    // Create a new UserClassLoader. This class loader handles blocking any disallowed
    // packages/classes.
    UserClassLoader urlClassLoader =
        new UserClassLoader(
            classLoaderUrls,
            JavaRunner.class.getClassLoader(),
            this.javaClassNames,
            permissionLevel);

    runner.run(urlClassLoader);

    try {
      urlClassLoader.close();
    } catch (IOException e) {
      // The user code has finished running. We don't want to confuse them at this point with an
      // error message.
      throw new InternalFacingException("Error closing urlClassLoader: " + e, e);
    }
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
