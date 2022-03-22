package org.code.javabuilder;

import java.util.List;
import org.code.protocol.OutputAdapter;
import org.code.protocol.StatusMessageKey;

/** Runs all tests for a given set of Java files */
public class TestRunner extends BaseTestRunner {

  public TestRunner(List<JavaProjectFile> javaFiles, OutputAdapter outputAdapter) {
    this(javaFiles, new JavabuilderTestExecutionListener(outputAdapter, false), outputAdapter);
  }

  TestRunner(
      List<JavaProjectFile> javaFiles,
      JavabuilderTestExecutionListener listener,
      OutputAdapter outputAdapter) {
    super(javaFiles, listener, outputAdapter, StatusMessageKey.RUNNING_PROJECT_TESTS);
  }
}
