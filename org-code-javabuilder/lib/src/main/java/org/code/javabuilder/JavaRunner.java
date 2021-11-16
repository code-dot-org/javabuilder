package org.code.javabuilder;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    this.run(this.mainRunner);
  }

  public void runTests() throws JavabuilderException, InternalFacingException {
    this.run(this.testRunner);
  }

  private void run(CodeRunner runner) throws JavabuilderException, InternalFacingException {
    // Include the user-facing api jars in the code we are loading so student code can access them.
    URL[] classLoaderUrls = Util.getAllJarURLs(this.executableLocation);

    // Create a new URLClassLoader. Use the current class loader as the parent so IO settings are
    // preserved.
    UserClassLoader urlClassLoader =
        new UserClassLoader(
            classLoaderUrls, JavaRunner.class.getClassLoader(), this.javaClassNames);

    runner.run(urlClassLoader);

    try {
      urlClassLoader.close();
    } catch (IOException e) {
      // The user code has finished running. We don't want to confuse them at this point with an
      // error message.
      throw new InternalFacingException("Error closing urlClassLoader: " + e, e);
    }
  }

  private List<String> parseClassNames(List<JavaProjectFile> javaFiles) {
    List<String> classNames = new ArrayList<>();
    for (JavaProjectFile file : javaFiles) {
      classNames.add(file.getClassName());
    }
    return classNames;
  }
}
