package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CodeBuilderTest {
  private CodeBuilder codeBuilder;

  @BeforeEach
  public void setUp() throws Exception {
    codeBuilder =
        new CodeBuilder(
            mock(InputAdapter.class), mock(OutputAdapter.class), mock(ProjectFileManager.class));
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
}
