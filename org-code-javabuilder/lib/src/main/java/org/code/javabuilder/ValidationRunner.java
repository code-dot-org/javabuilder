package org.code.javabuilder;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.code.javabuilder.util.ProjectLoadUtils;
import org.code.protocol.*;
import org.code.validation.support.NeighborhoodTracker;
import org.code.validation.support.ValidationProtocol;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

public class ValidationRunner implements CodeRunner {
  private final List<JavaProjectFile> validationFiles;
  private final List<JavaProjectFile> projectFiles;
  private final JavabuilderTestExecutionListener listener;
  private final OutputAdapter outputAdapter;

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
    this.validationFiles = validationFiles;
    this.projectFiles = projectFiles;
    this.listener = listener;
    this.outputAdapter = outputAdapter;
  }

  /**
   * Finds and runs all tests in the given set of Java files using the given URLClassLoader
   *
   * @param urlClassLoader class loader to load compiled classes
   * @throws InternalServerError if there is an error running tests
   */
  public void run(URLClassLoader urlClassLoader)
      throws InternalServerError, UserInitiatedException {
    try {
      this.setUpForValidation(urlClassLoader);
      // Search all project files for tests
      final List<ClassSelector> classSelectors = new ArrayList<>();
      for (JavaProjectFile file : this.validationFiles) {
        classSelectors.add(
            DiscoverySelectors.selectClass(urlClassLoader.loadClass(file.getClassName())));
      }
      final LauncherDiscoveryRequest request =
          LauncherDiscoveryRequestBuilder.request().selectors(classSelectors).build();

      try (LauncherSession session = LauncherFactory.openSession()) {
        final Launcher launcher = session.getLauncher();
        // Register listener
        launcher.registerTestExecutionListeners(this.listener);
        // Discover tests and build a test plan
        final TestPlan testPlan = launcher.discover(request);
        this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.RUNNING_VALIDATION));
        // Execute test plan
        launcher.execute(testPlan);
      }
    } catch (PreconditionViolationException | ClassNotFoundException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }

  private void setUpForValidation(URLClassLoader urlClassLoader) throws UserInitiatedException {
    Method mainMethod = ProjectLoadUtils.findMainMethod(urlClassLoader, this.projectFiles);
    ValidationProtocol.create(mainMethod, new NeighborhoodTracker());
  }
}
