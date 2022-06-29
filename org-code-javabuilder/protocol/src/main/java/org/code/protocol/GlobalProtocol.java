package org.code.protocol;

import java.util.HashSet;
import java.util.Set;

/**
 * This sets up the protocols that are used across jars in Javabuilder. It allows the input and
 * output adapters to be set at the entrypoint of the system (i.e. LambdaRequestHandler and
 * WebSocketServer) so they can be used by the various disconnected jars. This allows us to expose
 * our Neighborhood, Theater, and Park APIs to students without exposing all of our code. It also
 * allows these APIs to communicate with their Client-side counterparts while using the correct IO
 * adapters.
 */
public class GlobalProtocol extends JavabuilderSharedObject {
  private final OutputAdapter outputAdapter;
  private final InputHandler inputHandler;
  private final Set<MessageHandler> messageHandlers;
  private final LifecycleNotifier lifecycleNotifier;
  private final ContentManager contentManager;

  public GlobalProtocol(
      OutputAdapter outputAdapter,
      InputHandler inputHandler,
      LifecycleNotifier lifecycleNotifier,
      ContentManager contentManager) {
    this.outputAdapter = outputAdapter;
    this.inputHandler = inputHandler;
    this.messageHandlers = new HashSet<>();
    this.lifecycleNotifier = lifecycleNotifier;
    this.contentManager = contentManager;
  }

  public ContentManager getContentManager() {
    return this.contentManager;
  }

  public OutputAdapter getOutputAdapter() {
    return this.outputAdapter;
  }

  public InputHandler getInputHandler() {
    return this.inputHandler;
  }

  public void registerMessageHandler(MessageHandler handler) {
    this.messageHandlers.add(handler);
  }

  public void registerLifecycleListener(LifecycleListener listener) {
    this.lifecycleNotifier.registerListener(listener);
  }

  @Override
  // Clean up resources that require explicit clean up before exiting
  public void onExecutionEnded() {
    for (MessageHandler handler : this.messageHandlers) {
      handler.exit();
    }
  }
}
