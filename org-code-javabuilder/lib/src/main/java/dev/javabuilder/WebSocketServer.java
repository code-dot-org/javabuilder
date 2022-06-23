package dev.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.Base64;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.code.javabuilder.*;
import org.code.protocol.*;
import org.code.validation.support.UserTestOutputAdapter;
import org.json.JSONObject;

/**
 * This sets up a simple WebSocket server for local development when interactions between dashboard
 * and Javabuilder are needed. It expects a local instance of dashboard to be running. We also do
 * not account for multiple users or for auth here as would be normal on a WebSocket server. This is
 * because both of those use cases are handled by AWS API Gateway and should be tested with AWS SAM
 * or API Gateway directly.
 */
@ServerEndpoint("/javabuilder")
public class WebSocketServer {
  private static final String CONNECTED_MESSAGE = "CONNECTED";
  private WebSocketInputAdapter inputAdapter;
  private WebSocketOutputAdapter websocketOutputAdapter;
  private OutputAdapter outputAdapter;
  private Handler logHandler;
  private Logger logger;
  private CodeExecutionManager codeExecutionManager;
  private boolean finishedExecution;

  public WebSocketServer() {
    CachedResources.create();
  }

  /**
   * This acts as the main function for the WebSocket server. Therefore, we do many of the same
   * things here as we do for the LambdaRequestHandler, such as setting up the input and output
   * handlers. However, OnOpen needs to complete in order for the OnClose and OnMessage handlers to
   * be triggered. This is why we invoke the CodeBuilder in its own thread.
   *
   * @param session The individual WebSocket session.
   */
  @OnOpen
  public void onOpen(Session session) {
    this.finishedExecution = false;
    JavabuilderContext.getInstance().destroyAndReset();
    PerformanceTracker performanceTracker = new PerformanceTracker();
    JavabuilderContext.getInstance().register(PerformanceTracker.class, performanceTracker);
    performanceTracker.trackInstanceStart(Clock.systemUTC().instant());
    // Decode the authorization token
    String token = session.getRequestParameterMap().get("Authorization").get(0);
    Base64.Decoder decoder = Base64.getDecoder();
    String payload = new String(decoder.decode(token.split("\\.")[1]));
    JSONObject queryInput = new JSONObject(payload);

    final String connectionId = "LocalhostWebSocketConnection";
    final String levelId = queryInput.getString("level_id");
    final String channelId =
        queryInput.has("channel_id") ? queryInput.getString("channel_id") : "noneProvided";
    final ExecutionType executionType =
        ExecutionType.valueOf(queryInput.getString("execution_type"));
    final JSONObject options = new JSONObject(queryInput.getString("options"));
    final List<String> compileList = JSONUtils.listFromJSONObjectMember(options, "compileList");

    this.logger = Logger.getLogger(MAIN_LOGGER);
    this.logHandler = new LocalLogHandler(System.out, levelId, channelId);
    this.logger.addHandler(this.logHandler);
    // turn off the default console logger
    this.logger.setUseParentHandlers(false);

    LocalMetricClient metricClient = new LocalMetricClient();
    JavabuilderContext.getInstance().register(MetricClient.class, metricClient);

    Properties.setConnectionId(connectionId);

    websocketOutputAdapter = new WebSocketOutputAdapter(session);
    inputAdapter = new WebSocketInputAdapter();
    outputAdapter = websocketOutputAdapter;

    if (executionType == ExecutionType.TEST) {
      outputAdapter = new UserTestOutputAdapter(websocketOutputAdapter);
    }

    final ExceptionHandler exceptionHandler =
        new ExceptionHandler(outputAdapter, new LocalSystemExitHelper());
    // the code must be run in a thread so we can receive input messages
    Thread codeExecutor =
        new Thread(
            () -> {
              try {
                final LocalContentManager contentManager = new LocalContentManager();
                codeExecutionManager =
                    new CodeExecutionManager(
                        contentManager.getProjectFileLoader(),
                        inputAdapter,
                        outputAdapter,
                        executionType,
                        compileList,
                        new LocalTempDirectoryManager(),
                        contentManager,
                        new LocalSystemExitHelper());
                codeExecutionManager.execute();
              } catch (Throwable e) {
                // Catch all exceptions
                exceptionHandler.handle(e);
              }
              if (codeExecutionManager != null) {
                codeExecutionManager.shutDown();
              }
              this.finishedExecution = true;
              // Clean up session
              try {
                session.close();
                logger.removeHandler(this.logHandler);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });

    codeExecutor.start();
  }

  @OnClose
  public void myOnClose() {
    Logger.getLogger(MAIN_LOGGER).info("WebSocket closed.");
    PerformanceTracker performanceTracker =
        (PerformanceTracker) JavabuilderContext.getInstance().get(PerformanceTracker.class);
    performanceTracker.logPerformance();
    // If the websocket was closed before execution was finished, make sure we clean up.
    if (!this.finishedExecution) {
      if (codeExecutionManager != null) {
        this.codeExecutionManager.shutDown();
      }
      this.logger.removeHandler(this.logHandler);
    }
    JavabuilderContext.getInstance().destroyAndReset();
  }

  /**
   * Currently, the only way we accept messages from the client. This mimics console input.
   *
   * @param message The message from the client.
   */
  @OnMessage
  public void textMessage(String message) {
    // Ignore the initial "CONNECTED" message
    if (message.equals(CONNECTED_MESSAGE)) {
      return;
    }
    inputAdapter.appendMessage(message);
  }

  @OnMessage
  public void byteMessage(ByteBuffer b) {
    outputAdapter.sendMessage(new SystemOutMessage("Got a byte array message. Doing nothing."));
  }

  @OnMessage
  public void pongMessage(PongMessage p) {
    outputAdapter.sendMessage(new SystemOutMessage("Got a pong message. Doing nothing."));
  }
}
