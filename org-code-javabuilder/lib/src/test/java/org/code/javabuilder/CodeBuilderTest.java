package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.code.protocol.GlobalProtocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CodeBuilderTest {
  private UserProjectFiles userProjectFiles;
  private CodeBuilder codeBuilder;

  @BeforeEach
  public void setUp() throws Exception {
    userProjectFiles = mock(UserProjectFiles.class);
    codeBuilder = new CodeBuilder(mock(GlobalProtocol.class), userProjectFiles);
  }

  @Test
  void replacesSystemIOWhenCloseCalled() {
    PrintStream sysout = System.out;
    InputStream sysin = System.in;
    System.setOut(mock(PrintStream.class));
    System.setIn(mock(InputStream.class));
    try {
      codeBuilder.close();
    } catch (InternalFacingException e) {
      // Ignore for this test
    }
    assertEquals(sysout, System.out);
    assertEquals(sysin, System.in);
  }

  @Test
  public void testBuildUserCodeThrowsExceptionIfNoFilesToCompile() {
    final List<String> compileList = new ArrayList<>();
    when(userProjectFiles.getMatchingJavaFiles(compileList)).thenReturn(new ArrayList<>());

    final Exception exception =
        assertThrows(UserInitiatedException.class, () -> codeBuilder.buildUserCode(compileList));
    assertEquals(UserInitiatedExceptionKey.NO_FILES_TO_COMPILE.toString(), exception.getMessage());
  }
}
