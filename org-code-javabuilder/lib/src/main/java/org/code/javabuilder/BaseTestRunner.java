package org.code.javabuilder;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.OutputAdapter;
import org.code.protocol.StatusMessage;
import org.code.protocol.StatusMessageKey;
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

  public BaseTestRunner(
      List<JavaProjectFile> files,
      JavabuilderTestExecutionListener listener,
      OutputAdapter outputAdapter,
      StatusMessageKey statusMessageKey) {
    this.files = files;
    this.listener = listener;
    this.outputAdapter = outputAdapter;
    this.statusMessageKey = statusMessageKey;
  }

  /**
   * Finds and runs all tests in the given set of Java files using the given URLClassLoader
   *
   * @param urlClassLoader class loader to load compiled classes
   * @throws InternalServerError if there is an error running tests
   */
  @Override
  public void run(URLClassLoader urlClassLoader)
      throws InternalServerError, UserInitiatedException {
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
        this.outputAdapter.sendMessage(new StatusMessage(this.statusMessageKey));
        // Execute test plan
        launcher.execute(testPlan);
      }
    } catch (PreconditionViolationException | ClassNotFoundException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }
}
