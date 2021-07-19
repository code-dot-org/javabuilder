package org.code.protocol;

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
  private final InputAdapter inputAdapter;
  private final JavabuilderFileWriter fileWriter;
  private final String dashboardHostname;
  private final String channelId;

  private GlobalProtocol(
      OutputAdapter outputAdapter,
      InputAdapter inputAdapter,
      String dashboardHostname,
      String channelId,
      JavabuilderFileWriter fileWriter) {
    this.outputAdapter = outputAdapter;
    this.inputAdapter = inputAdapter;
    this.dashboardHostname = dashboardHostname;
    this.channelId = channelId;
    this.fileWriter = fileWriter;
  }

  public static void create(
      OutputAdapter outputAdapter,
      InputAdapter inputAdapter,
      String dashboardHostname,
      String channelId,
      JavabuilderFileWriter fileWriter) {
    GlobalProtocol.protocolInstance =
        new GlobalProtocol(outputAdapter, inputAdapter, dashboardHostname, channelId, fileWriter);
  }

  public static GlobalProtocol getInstance() {
    if (GlobalProtocol.protocolInstance == null) {
      throw new InternalJavabuilderError(InternalErrorKey.INTERNAL_EXCEPTION);
    }

    return GlobalProtocol.protocolInstance;
  }

  public OutputAdapter getOutputAdapter() {
    return this.outputAdapter;
  }

  public InputAdapter getInputAdapter() {
    return this.inputAdapter;
  }

  public JavabuilderFileWriter getFileWriter() {
    return this.fileWriter;
  }

  public String generateAssetUrl(String filename) {
    return String.format("%s/v3/assets/%s/%s", this.dashboardHostname, this.channelId, filename);
  }

  public String generateSourcesUrl() {
    return String.format("%s/v3/sources/%s", this.dashboardHostname, this.channelId);
  }

  public String getDashboardHostname() {
    return this.dashboardHostname;
  }
}
