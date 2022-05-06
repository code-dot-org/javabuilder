package org.code.javabuilder;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.List;
import org.code.javabuilder.util.ProjectLoadUtils;
import org.code.protocol.*;
import org.code.validation.support.NeighborhoodTracker;
import org.code.validation.support.ValidationProtocol;

public class ValidationRunner extends BaseTestRunner {
  private final List<JavaProjectFile> projectFiles;

  public ValidationRunner(
      List<JavaProjectFile> validationFiles,
      List<JavaProjectFile> projectFiles,
      OutputAdapter outputAdapter) {
    this(
        validationFiles,
        projectFiles,
        new JavabuilderTestExecutionListener(outputAdapter, true),
        outputAdapter);
  }

  ValidationRunner(
      List<JavaProjectFile> validationFiles,
      List<JavaProjectFile> projectFiles,
      JavabuilderTestExecutionListener listener,
      OutputAdapter outputAdapter) {
    super(validationFiles, listener, outputAdapter, true);
    this.projectFiles = projectFiles;
  }

  /**
   * Finds and runs all tests in the given set of Java files using the given URLClassLoader
   *
   * @param urlClassLoader class loader to load compiled classes
   * @throws InternalServerException if there is an error running tests
   */
  @Override
  public boolean run(URLClassLoader urlClassLoader)
      throws InternalServerException, UserInitiatedException {
    this.setUpForValidation(urlClassLoader);
    return super.run(urlClassLoader);
  }

  private void setUpForValidation(URLClassLoader urlClassLoader) throws UserInitiatedException {
    Method mainMethod = ProjectLoadUtils.findMainMethod(urlClassLoader, this.projectFiles);
    ValidationProtocol.create(mainMethod, new NeighborhoodTracker());
  }
}
