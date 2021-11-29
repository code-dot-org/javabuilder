package org.code.protocol;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.code.javabuilder.ContentManager;

/**
 * This sets up the protocols that are used across jars in Javabuilder. It allows the input and
 * output adapters to be set at the entrypoint of the system (i.e. LambdaRequestHandler, LocalMain,
 * and WebSocketServer) so they can be used by the various disconnected jars. This allows us to
 * expose our Neighborhood, Theater, and Park APIs to students without exposing all of our code. It
 * also allows these APIs to communicate with their Client-side counterparts while using the correct
 * IO adapters.
 */
public class GlobalProtocol {
  private static GlobalProtocol protocolInstance;
  private final OutputAdapter outputAdapter;
  private final InputHandler inputHandler;
  private final JavabuilderFileManager fileManager;
  private final String dashboardHostname;
  private final String channelId;
  private final AssetFileHelper assetFileHelper;
  private Set<MessageHandler> messageHandlers;
  private final ContentManager contentManager;

  private GlobalProtocol(
      OutputAdapter outputAdapter,
      InputHandler inputHandler,
      String dashboardHostname,
      String channelId,
      JavabuilderFileManager fileManager,
      AssetFileHelper assetFileHelper,
      ContentManager contentManager) {
    this.outputAdapter = outputAdapter;
    this.inputHandler = inputHandler;
    this.dashboardHostname = dashboardHostname;
    this.channelId = channelId;
    this.fileManager = fileManager;
    this.assetFileHelper = assetFileHelper;
    this.messageHandlers = new HashSet<>();
    this.contentManager = contentManager;
  }

  public static void create(
      OutputAdapter outputAdapter,
      InputAdapter inputAdapter,
      String dashboardHostname,
      String channelId,
      String levelId,
      JavabuilderFileManager fileManager,
      ContentManager contentManager) {
    GlobalProtocol.protocolInstance =
        new GlobalProtocol(
            outputAdapter,
            new InputHandler(inputAdapter),
            dashboardHostname,
            channelId,
            fileManager,
            new AssetFileHelper(dashboardHostname, channelId, levelId),
            contentManager);
  }

  // prototype - leaving this create method around so tests still compile
  public static void create(
      OutputAdapter outputAdapter,
      InputAdapter inputAdapter,
      String dashboardHostname,
      String channelId,
      String levelId,
      JavabuilderFileManager fileManager) {
    GlobalProtocol.protocolInstance =
        new GlobalProtocol(
            outputAdapter,
            new InputHandler(inputAdapter),
            dashboardHostname,
            channelId,
            fileManager,
            new AssetFileHelper(dashboardHostname, channelId, levelId),
            null);
  }

  public static GlobalProtocol getInstance() {
    if (GlobalProtocol.protocolInstance == null) {
      throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_EXCEPTION);
    }

    return GlobalProtocol.protocolInstance;
  }

  public OutputAdapter getOutputAdapter() {
    return this.outputAdapter;
  }

  public InputHandler getInputHandler() {
    return this.inputHandler;
  }

  public JavabuilderFileManager getFileManager() {
    return this.fileManager;
  }

  public ContentManager getContentManager() {
    return this.contentManager;
  }

  //  public String generateAssetUrl(String filename) {
  //    return this.contentManager.getAssetUrl(
  //        filename); // this.assetFileHelper.generateAssetUrl(filename);
  //  }

  public InputStream getAssetInputStream(String filename) {
    return this.contentManager.getAssetInputStream(filename);
  }

  public AssetFileHelper getAssetFileHelper() {
    return this.assetFileHelper;
  }

  public String generateSourcesUrl() {
    return String.format("%s/v3/sources/%s", this.dashboardHostname, this.channelId);
  }

  public void registerMessageHandler(MessageHandler handler) {
    this.messageHandlers.add(handler);
  }

  // Clean up resources that require explicit clean up before exiting
  public void cleanUpResources() {
    for (MessageHandler handler : this.messageHandlers) {
      handler.exit();
    }
  }
}
