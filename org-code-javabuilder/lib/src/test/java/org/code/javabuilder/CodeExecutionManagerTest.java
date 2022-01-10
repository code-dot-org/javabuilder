package org.code.javabuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
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
    codeBuilderRunnableFactory = mock(CodeBuilderRunnableFactory.class);
    codeBuilderRunnable = mock(CodeBuilderRunnable.class);

    when(codeBuilderRunnableFactory.createCodeBuilderRunnable(
            eq(fileLoader), eq(outputAdapter), any(File.class), eq(executionType), eq(compileList)))
        .thenReturn(codeBuilderRunnable);

    GlobalProtocol.create(outputAdapter, mock(InputAdapter.class), "", "", "", fileManager);

    unitUnderTest =
        new CodeExecutionManager(
            fileLoader,
            inputHandler,
            outputAdapter,
            executionType,
            compileList,
            fileManager,
            codeBuilderRunnableFactory);
  }

  @Test
  public void testExitEarlyDoesNothingIfExecutionFinished() throws IOException {
    unitUnderTest.execute();
    verify(codeBuilderRunnable).run();
    // Verify post-execute
    verify(fileManager, times(1)).cleanUpTempDirectory(any(File.class));

    unitUnderTest.requestEarlyExit();
    // Should not call post-execute again
    verify(fileManager, times(1)).cleanUpTempDirectory(any(File.class));
  }

  @Test
  public void testPostExecuteCalledOnlyOnceIfEarlyExitRequested() throws IOException {
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
    verify(fileManager, times(1)).cleanUpTempDirectory(any(File.class));
  }
}
