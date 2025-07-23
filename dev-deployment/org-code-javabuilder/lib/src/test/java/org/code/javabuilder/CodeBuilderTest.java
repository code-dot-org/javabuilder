package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.code.protocol.GlobalProtocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CodeBuilderTest {
  private UserProjectFiles userProjectFiles;
  private GlobalProtocol globalProtocol;
  private File tempFolder;
  private CodeBuilder codeBuilder;

  @BeforeEach
  public void setUp() throws Exception {
    globalProtocol = mock(GlobalProtocol.class);
    tempFolder = mock(File.class);
    userProjectFiles = mock(UserProjectFiles.class);

    codeBuilder =
        new CodeBuilder(globalProtocol, userProjectFiles, mock(UserProjectFiles.class), tempFolder);
  }

  @Test
  public void testBuildUserCodeThrowsExceptionFileListIsNull() {
    final Exception exception =
        assertThrows(UserInitiatedException.class, () -> codeBuilder.buildUserCode(null));
    assertEquals(UserInitiatedExceptionKey.NO_FILES_TO_COMPILE.toString(), exception.getMessage());
    verify(userProjectFiles, never()).getMatchingJavaFiles(anyList());
  }

  @Test
  public void testBuildUserCodeThrowsExceptionIfNoFilesToCompile() {
    final List<String> compileList = new ArrayList<>();
    when(userProjectFiles.getMatchingJavaFiles(compileList)).thenReturn(new ArrayList<>());

    final Exception exception =
        assertThrows(UserInitiatedException.class, () -> codeBuilder.buildUserCode(compileList));
    assertEquals(UserInitiatedExceptionKey.NO_FILES_TO_COMPILE.toString(), exception.getMessage());
  }

  @Test
  public void testBuildAllUserCodeThrowsExceptionIfNoFilesToCompile() {
    when(userProjectFiles.getJavaFiles()).thenReturn(new ArrayList<>());
    final Exception exception =
        assertThrows(UserInitiatedException.class, () -> codeBuilder.buildAllUserCode());
    assertEquals(UserInitiatedExceptionKey.NO_FILES_TO_COMPILE.toString(), exception.getMessage());
  }
}
