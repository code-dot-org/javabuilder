package org.code.javabuilder;

import org.code.protocol.*;

public class CodeBuilderWrapper {
  private final ProjectFileLoader fileLoader;
  private final OutputAdapter outputAdapter;

  public CodeBuilderWrapper(ProjectFileLoader fileLoader, OutputAdapter outputAdapter) {
    this.fileLoader = fileLoader;
    this.outputAdapter = outputAdapter;
  }

  public void executeCodeBuilder() {
    try {
      UserProjectFiles userProjectFiles = fileLoader.loadFiles();
      try (CodeBuilder codeBuilder =
          new CodeBuilder(GlobalProtocol.getInstance(), userProjectFiles)) {
        codeBuilder.buildUserCode();
        codeBuilder.runUserCode();
      }
    } catch (JavabuilderException | JavabuilderRuntimeException e) {
      // The error affected the user. Tell them about it.
      outputAdapter.sendMessage(e.getExceptionMessage());
    } catch (InternalFacingException e) {
      // Errors we caused that don't affect the user.
      // These have already been logged. There's nothing else to do here.
    } catch (Throwable e) {
      // Errors we didn't catch. These may have affected the user. For now, let's tell
      // them about it.
      outputAdapter.sendMessage(
          (new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION, e)).getExceptionMessage());
    }
  }
}
