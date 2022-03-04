package dev.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.util.logging.Logger;
import org.code.javabuilder.*;
import org.code.protocol.*;

/**
 * Intended for local testing only. This is a local version of the Javabuilder lambda function. The
 * LocalInputAdapter can be used to pass input to the program. The "MyClass.java" program in the
 * resources folder is the "user program." Output goes to the console.
 */
public class LocalMain {
  public static void main(String[] args) throws InterruptedException {
    final LocalInputAdapter inputAdapter = new LocalInputAdapter();
    final LocalOutputAdapter outputAdapter = new LocalOutputAdapter(System.out);

    Logger logger = Logger.getLogger(MAIN_LOGGER);
    logger.addHandler(new LocalLogHandler(System.out, "levelId", "channelId", "miniAppType"));
    // turn off the default console logger
    logger.setUseParentHandlers(false);

    GlobalProtocol.create(
        outputAdapter, inputAdapter, new LifecycleNotifier(), new LocalContentManager());
    CachedResources.create();

    // Create and invoke the code execution environment
    CodeExecutionManager codeExecutionManager =
        new CodeExecutionManager(
            new LocalContentManager(),
            GlobalProtocol.getInstance().getInputHandler(),
            outputAdapter,
            ExecutionType.RUN,
            null,
            new LocalTempDirectoryManager(),
            new LifecycleNotifier());
    codeExecutionManager.execute();
  }
}
