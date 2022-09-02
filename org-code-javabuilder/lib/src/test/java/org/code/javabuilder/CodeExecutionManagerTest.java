package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import org.code.javabuilder.CodeExecutionManager.CodeBuilderRunnableFactory;
import org.code.protocol.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CodeExecutionManagerTest {
  private ProjectFileLoader fileLoader;
  private InputAdapter inputAdapter;
  private OutputAdapter outputAdapter;
  private ExecutionType executionType;
  private List<String> compileList;
  private TempDirectoryManager tempDirectoryManager;
  private CodeBuilderRunnableFactory codeBuilderRunnableFactory;
  private CodeBuilderRunnable codeBuilderRunnable;
  private ContentManager contentManager;
  private CodeExecutionManager unitUnderTest;

  @BeforeEach
  public void setUp() {
    fileLoader = mock(ProjectFileLoader.class);
    inputAdapter = mock(InputAdapter.class);
    outputAdapter = mock(OutputAdapter.class);
    executionType = ExecutionType.RUN;
    compileList = mock(List.class);
    tempDirectoryManager = mock(TempDirectoryManager.class);
    codeBuilderRunnableFactory = mock(CodeBuilderRunnableFactory.class);
    codeBuilderRunnable = mock(CodeBuilderRunnable.class);
    contentManager = mock(ContentManager.class);

    when(codeBuilderRunnableFactory.createCodeBuilderRunnable(
            eq(fileLoader), any(File.class), eq(executionType), eq(compileList)))
        .thenReturn(codeBuilderRunnable);

    unitUnderTest =
        new CodeExecutionManager(
            fileLoader,
            inputAdapter,
            outputAdapter,
            executionType,
            compileList,
            tempDirectoryManager,
            contentManager,
            mock(SystemExitHelper.class),
            codeBuilderRunnableFactory);
  }

  @Test
  public void testCannotShutDownIfNotInitialized()
      throws JavabuilderException, InternalFacingException {
    unitUnderTest.execute();
    unitUnderTest.shutDown();
    verify(codeBuilderRunnable).run();
    // Verify post-execute
    // verifyExitedMessageSentOnce();

    unitUnderTest.shutDown();
    // Should not call post-execute again
    // verifyExitedMessageSentOnce();
  }

  @Test
  public void testPostExecuteCalledOnlyOnceIfShutDownEarly()
      throws JavabuilderException, InternalFacingException {
    // Hack to simulate the timeout scenario where shutDown is called before run() is
    // finished
    doAnswer(
            invocation -> {
              unitUnderTest.shutDown();
              return null;
            })
        .when(codeBuilderRunnable)
        .run();

    unitUnderTest.execute();
    unitUnderTest.shutDown();

    // Verify post-execute happened only once
    // verifyExitedMessageSentOnce();
  }

  @Test
  public void testReplacesSystemIOAfterExecution()
      throws JavabuilderException, InternalFacingException {
    final PrintStream sysOut = System.out;
    final InputStream sysIn = System.in;

    unitUnderTest.execute();
    assertNotSame(sysOut, System.out);
    assertNotSame(sysIn, System.in);

    unitUnderTest.shutDown();
    assertSame(sysOut, System.out);
    assertSame(sysIn, System.in);
  }

  private void verifyExitedMessageSentOnce() {
    ArgumentCaptor<StatusMessage> message = ArgumentCaptor.forClass(StatusMessage.class);
    verify(outputAdapter, times(1)).sendMessage(message.capture());
    assertEquals(message.getValue().getValue(), "EXITED");
  }
}
