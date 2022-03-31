package org.code.javabuilder;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.OutputAdapter;
import org.code.protocol.StatusMessage;
import org.code.protocol.StatusMessageKey;
import org.code.validation.support.UserTestOutputAdapter;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

public class BaseTestRunner implements CodeRunner {
  private final List<JavaProjectFile> files;
  private final JavabuilderTestExecutionListener listener;
  private final OutputAdapter outputAdapter;
  private final StatusMessageKey statusMessageKey;
  private final boolean isValidation;

  public BaseTestRunner(
      List<JavaProjectFile> files,
      JavabuilderTestExecutionListener listener,
      OutputAdapter outputAdapter,
      boolean isValidation) {
    this.files = files;
    this.listener = listener;
    this.outputAdapter = outputAdapter;
    this.statusMessageKey =
        isValidation ? StatusMessageKey.RUNNING_VALIDATION : StatusMessageKey.RUNNING_PROJECT_TESTS;
    this.isValidation = isValidation;
  }

  /**
   * Finds and runs all tests in the given set of Java files using the given URLClassLoader
   *
   * @param urlClassLoader class loader to load compiled classes
   * @throws InternalServerError if there is an error running tests
   * @return true if there were tests to run, false if there were not.
   */
  @Override
  public boolean run(URLClassLoader urlClassLoader)
      throws InternalServerError, UserInitiatedException {
    if (outputAdapter instanceof UserTestOutputAdapter) {
      ((UserTestOutputAdapter) outputAdapter).setIsValidation(this.isValidation);
    }
    if (this.files.size() == 0) {
      return false;
    }
    try {
      // Search all project files for tests
      final List<ClassSelector> classSelectors = new ArrayList<>();
      for (JavaProjectFile file : this.files) {
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
        if (!testPlan.containsTests()) {
          return false;
        }
        this.outputAdapter.sendMessage(new StatusMessage(this.statusMessageKey));
        // Execute test plan
        launcher.execute(testPlan);
        return true;
      }
    } catch (PreconditionViolationException | ClassNotFoundException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }
}
