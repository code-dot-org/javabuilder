package org.code.javabuilder;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.code.javabuilder.util.ProjectLoadUtils;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.OutputAdapter;
import org.code.protocol.StatusMessage;
import org.code.protocol.StatusMessageKey;
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

/** Runs all tests for a given set of Java files */
public class TestRunner implements CodeRunner {
  private final List<JavaProjectFile> javaFiles;
  private final JavabuilderTestExecutionListener listener;
  private final OutputAdapter outputAdapter;

  public TestRunner(List<JavaProjectFile> javaFiles, OutputAdapter outputAdapter) {
    // TODO: isValidation will need to be controllable at the constructor level once we are running
    // validation code separately.
    this(javaFiles, new JavabuilderTestExecutionListener(outputAdapter, false), outputAdapter);
  }

  TestRunner(
      List<JavaProjectFile> javaFiles,
      JavabuilderTestExecutionListener listener,
      OutputAdapter outputAdapter) {
    this.javaFiles = javaFiles;
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
      // TODO: only set up for validation if we know we have validation code to run. For now, both
      // project tests and validation code are run together.
      this.setUpForValidation(urlClassLoader);
      // Search all project files for tests
      final List<ClassSelector> classSelectors = new ArrayList<>();
      for (JavaProjectFile file : this.javaFiles) {
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
        // TODO: when we run validation separately, send RUNNING_VALIDATION status message.
        this.outputAdapter.sendMessage(new StatusMessage(StatusMessageKey.RUNNING_PROJECT_TESTS));
        // Execute test plan
        launcher.execute(testPlan);
      }
    } catch (PreconditionViolationException | ClassNotFoundException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }

  private void setUpForValidation(URLClassLoader urlClassLoader) throws UserInitiatedException {
    Method mainMethod = ProjectLoadUtils.findMainMethod(urlClassLoader, this.javaFiles);
    // TODO: create NeighborhoodTracker instance and save it to validation protocol.
    ValidationProtocol.create(mainMethod);
  }
}
