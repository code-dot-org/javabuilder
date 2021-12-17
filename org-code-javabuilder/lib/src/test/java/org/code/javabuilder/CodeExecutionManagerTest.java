package org.code.javabuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.List;
import org.code.javabuilder.CodeExecutionManager.CodeBuilderRunnableFactory;
import org.code.protocol.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CodeExecutionManagerTest {
  private ProjectFileLoader fileLoader;
  private InputHandler inputHandler;
  private OutputAdapter outputAdapter;
  private ExecutionType executionType;
  private List<String> compileList;
  private JavabuilderFileManager fileManager;
  private LifecycleNotifier lifecycleNotifier;
  private CodeBuilderRunnableFactory codeBuilderRunnableFactory;
  private CodeBuilderRunnable codeBuilderRunnable;
  private CodeExecutionManager unitUnderTest;

  @BeforeEach
  public void setUp() {
    fileLoader = mock(ProjectFileLoader.class);
    inputHandler = mock(InputHandler.class);
    outputAdapter = mock(OutputAdapter.class);
    executionType = ExecutionType.RUN;
    compileList = mock(List.class);
    fileManager = mock(JavabuilderFileManager.class);
    lifecycleNotifier = mock(LifecycleNotifier.class);
    codeBuilderRunnableFactory = mock(CodeBuilderRunnableFactory.class);
    codeBuilderRunnable = mock(CodeBuilderRunnable.class);

    when(codeBuilderRunnableFactory.createCodeBuilderRunnable(
            eq(fileLoader), eq(outputAdapter), any(File.class), eq(executionType), eq(compileList)))
        .thenReturn(codeBuilderRunnable);

    GlobalProtocol.create(
        outputAdapter, mock(InputAdapter.class), "", "", "", fileManager, lifecycleNotifier);

    unitUnderTest =
        new CodeExecutionManager(
            fileLoader,
            inputHandler,
            outputAdapter,
            executionType,
            compileList,
            fileManager,
            lifecycleNotifier,
            codeBuilderRunnableFactory);
  }

  @Test
  public void testExitEarlyDoesNothingIfExecutionFinished() {
    unitUnderTest.execute();
    verify(codeBuilderRunnable).run();
    // Verify post-execute
    verify(lifecycleNotifier, times(1)).onExecutionEnded();

    unitUnderTest.requestEarlyExit();
    // Should not call post-execute again
    verify(lifecycleNotifier, times(1)).onExecutionEnded();
  }

  @Test
  public void testPostExecuteCalledOnlyOnceIfEarlyExitRequested() {
    // Hack to simulate the timeout scenario where requestEarlyExit is called before run() is
    // finished
    doAnswer(
            invocation -> {
              unitUnderTest.requestEarlyExit();
              return null;
            })
        .when(codeBuilderRunnable)
        .run();

    unitUnderTest.execute();

    // Verify post-execute happened only once
    verify(lifecycleNotifier, times(1)).onExecutionEnded();
  }
}
