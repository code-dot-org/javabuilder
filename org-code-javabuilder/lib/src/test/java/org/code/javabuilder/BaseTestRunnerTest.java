package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URLClassLoader;
import java.util.List;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.OutputAdapter;
import org.code.protocol.StatusMessageKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class BaseTestRunnerTest {
  private JavaProjectFile file1;
  private JavaProjectFile file2;
  private Class javaClass1;
  private Class javaClass2;
  private ClassSelector classSelector1;
  private ClassSelector classSelector2;

  private JavabuilderTestExecutionListener listener;
  private URLClassLoader urlClassLoader;
  private LauncherDiscoveryRequestBuilder requestBuilderInstance;
  private LauncherDiscoveryRequest discoveryRequest;
  private LauncherSession launcherSession;
  private Launcher launcher;
  private TestPlan testPlan;
  private ArgumentCaptor<List<ClassSelector>> classSelectorsCaptor;

  private MockedStatic<DiscoverySelectors> discoverySelectors;
  private MockedStatic<LauncherDiscoveryRequestBuilder> requestBuilder;
  private MockedStatic<LauncherFactory> launcherFactory;

  private BaseTestRunner unitUnderTest;

  /**
   * Suppresses warnings for instantiating classSelectorsCaptor with type List instead of
   * List<ClassSelectors>; not relevant for this test
   */
  @BeforeEach
  @SuppressWarnings("unchecked")
  public void setUp() throws UserInitiatedException {
    file1 = new JavaProjectFile("file1.java");
    file2 = new JavaProjectFile("file2.java");
    javaClass1 = String.class;
    javaClass2 = Integer.class;
    classSelector1 = mock(ClassSelector.class);
    classSelector2 = mock(ClassSelector.class);

    listener = mock(JavabuilderTestExecutionListener.class);
    urlClassLoader = mock(URLClassLoader.class);
    requestBuilderInstance = mock(LauncherDiscoveryRequestBuilder.class);
    discoveryRequest = mock(LauncherDiscoveryRequest.class);
    launcherSession = mock(LauncherSession.class);
    launcher = mock(Launcher.class);
    testPlan = mock(TestPlan.class);
    classSelectorsCaptor = ArgumentCaptor.forClass(List.class);

    discoverySelectors = mockStatic(DiscoverySelectors.class);
    requestBuilder = mockStatic(LauncherDiscoveryRequestBuilder.class);
    launcherFactory = mockStatic(LauncherFactory.class);

    unitUnderTest =
        new BaseTestRunner(
            List.of(file1, file2),
            listener,
            mock(OutputAdapter.class),
            StatusMessageKey.RUNNING_PROJECT_TESTS);
  }

  @AfterEach
  public void tearDown() {
    discoverySelectors.close();
    requestBuilder.close();
    launcherFactory.close();
  }

  /** Suppresses warning for casting Class to Class<?>; not relevant for this test */
  @Test
  @SuppressWarnings("unchecked")
  public void testRunFindsAndRunsAllTests()
      throws InternalServerError, ClassNotFoundException, UserInitiatedException {
    when(urlClassLoader.loadClass(file1.getClassName())).thenReturn(javaClass1);
    when(urlClassLoader.loadClass(file2.getClassName())).thenReturn(javaClass2);
    discoverySelectors
        .when(() -> DiscoverySelectors.selectClass(javaClass1))
        .thenReturn(classSelector1);
    discoverySelectors
        .when(() -> DiscoverySelectors.selectClass(javaClass2))
        .thenReturn(classSelector2);

    requestBuilder
        .when(LauncherDiscoveryRequestBuilder::request)
        .thenReturn(requestBuilderInstance);
    when(requestBuilderInstance.selectors(anyList())).thenReturn(requestBuilderInstance);
    when(requestBuilderInstance.build()).thenReturn(discoveryRequest);

    launcherFactory.when(LauncherFactory::openSession).thenReturn(launcherSession);
    when(launcherSession.getLauncher()).thenReturn(launcher);
    when(launcher.discover(discoveryRequest)).thenReturn(testPlan);
    when(testPlan.containsTests()).thenReturn(true);

    unitUnderTest.run(urlClassLoader);

    verify(urlClassLoader, times(1)).loadClass(file1.getClassName());
    verify(urlClassLoader, times(1)).loadClass(file2.getClassName());
    verify(requestBuilderInstance).selectors(classSelectorsCaptor.capture());

    final List<ClassSelector> classSelectors = classSelectorsCaptor.getValue();
    assertTrue(classSelectors.contains(classSelector1));
    assertTrue(classSelectors.contains(classSelector2));

    verify(launcher).registerTestExecutionListeners(listener);
    verify(launcher).execute(testPlan);
  }

  @Test
  public void testRunThrowsExceptionIfClassNotFound() throws ClassNotFoundException {
    final ClassNotFoundException cause = new ClassNotFoundException();
    when(urlClassLoader.loadClass(file1.getClassName())).thenThrow(cause);

    final Exception actual =
        assertThrows(InternalServerError.class, () -> unitUnderTest.run(urlClassLoader));

    assertEquals(InternalErrorKey.INTERNAL_EXCEPTION.toString(), actual.getMessage());
    assertSame(cause, actual.getCause());
  }
}
