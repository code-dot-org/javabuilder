package dev.javabuilder;

import java.nio.ByteBuffer;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.code.javabuilder.*;

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
    // Temporary project url until the dashboard side is integrated
    String projectUrl = "http://localhost-studio.code.org:3000/v3/files/MoGwvaZmQSQZaImS3LliCQ";
    String[] fileNames = new String[] {"MyClass.java"};

    outputAdapter = new WebSocketOutputAdapter(session);
    inputAdapter = new WebSocketInputAdapter();
    final UserProjectFileManager fileManager = new UserProjectFileManager(projectUrl, fileNames);
    Thread codeExecutor =
        new Thread(
            () -> {
              try (CodeBuilder codeBuilder =
                  new CodeBuilder(inputAdapter, outputAdapter, fileManager)) {
                codeBuilder.compileUserCode();
                codeBuilder.runUserCode();
              } catch (UserFacingException e) {
                outputAdapter.sendMessage(e.getMessage());
                outputAdapter.sendMessage("\n" + e.getLoggingString());
              } catch (UserInitiatedException e) {
                outputAdapter.sendMessage(e.getMessage());
                outputAdapter.sendMessage("\n" + e.getLoggingString());
              } catch (InternalFacingException e) {
                outputAdapter.sendMessage("\n" + e.getLoggingString());
              }
            });
    codeExecutor.start();
  }

  @OnClose
  public void myOnClose() {
    System.out.println("Session Closed");
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
    outputAdapter.sendMessage("Got a byte array message. Doing nothing.");
  }

  @OnMessage
  public void pongMessage(PongMessage p) {
    outputAdapter.sendMessage("Got a pong message. Doing nothing.");
  }
}
