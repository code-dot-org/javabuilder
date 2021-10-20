package dev.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import java.io.IOException;
import java.nio.ByteBuffer;
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
  private WebSocketInputAdapter inputAdapter;
  private WebSocketOutputAdapter outputAdapter;
  private Handler logHandler;
  private Logger logger;
  private Thread codeExecutor;

  /**
   * This acts as the main function for the WebSocket server. Therefore, we do many of the same
   * things here as we do for LocalMain or for the LambdaRequestHandler, such as setting up the
   * input and output handlers. However, OnOpen needs to complete in order for the OnClose and
   * OnMessage handlers to be triggered. This is why we invoke the CodeBuilder in its own thread.
   *
   * @param session The individual WebSocket session.
   */
  @OnOpen
  public void onOpen(Session session) {
    // Decode the authorization token
    String token = session.getRequestParameterMap().get("Authorization").get(0);
    Base64.Decoder decoder = Base64.getDecoder();
    String payload = new String(decoder.decode(token.split("\\.")[1]));
    JSONObject queryInput = new JSONObject(payload);

    final String connectionId = "LocalhostWebSocketConnection";
    final String levelId = queryInput.getString("level_id");
    final String channelId = queryInput.getString("channel_id");
    final ExecutionType executionType =
        ExecutionType.valueOf(queryInput.getString("execution_type"));
    final String dashboardHostname = "http://" + queryInput.get("iss") + ":3000";
    final JSONObject options = new JSONObject(queryInput.getString("options"));
    final boolean useNeighborhood =
        JSONUtils.booleanFromJSONObjectMember(options, "useNeighborhood");
    final List<String> compileList = JSONUtils.listFromJSONObjectMember(options, "compileList");

    this.logger = Logger.getLogger(MAIN_LOGGER);
    this.logHandler = new LocalLogHandler(System.out, levelId, channelId);
    this.logger.addHandler(this.logHandler);
    // turn off the default console logger
    this.logger.setUseParentHandlers(false);

    Properties.setConnectionId(connectionId);

    outputAdapter = new WebSocketOutputAdapter(session);
    inputAdapter = new WebSocketInputAdapter();
    GlobalProtocol.create(
        outputAdapter, inputAdapter, dashboardHostname, channelId, levelId, new LocalFileWriter());
    final UserProjectFileLoader fileLoader =
        new UserProjectFileLoader(
            GlobalProtocol.getInstance().generateSourcesUrl(),
            levelId,
            dashboardHostname,
            useNeighborhood);
    CodeBuilderRunnable codeBuilderRunnable =
        new CodeBuilderRunnable(fileLoader, outputAdapter, executionType, compileList);
    this.codeExecutor = new Thread(codeBuilderRunnable);
    this.codeExecutor.start();
    Thread waitToCleanup =
        new Thread(
            () -> {
              try {
                this.codeExecutor.join();
              } catch (InterruptedException e) {
                // ignore this exception, it likely means the end user stopped
                // their program.
              }
              try {
                session.close();
                logger.removeHandler(this.logHandler);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
    waitToCleanup.start();
  }

  @OnClose
  public void myOnClose() {
    if (this.codeExecutor != null && this.codeExecutor.isAlive()) {
      this.codeExecutor.interrupt();
      Logger.getLogger(MAIN_LOGGER).info("WebSocket has been closed, interrupted running program");
    } else {
      Logger.getLogger(MAIN_LOGGER).info("WebSocket Closed");
    }
  }

  /**
   * Currently, the only way we accept messages from the client. This mimics console input.
   *
   * @param message The message from the client.
   */
  @OnMessage
  public void textMessage(String message) {
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
