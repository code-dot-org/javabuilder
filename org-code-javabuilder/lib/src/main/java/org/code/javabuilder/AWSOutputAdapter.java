package org.code.javabuilder;

import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.code.protocol.*;

/** Sends messages to Amazon API Gateway from the user's program. */
public class AWSOutputAdapter implements OutputAdapter {
  private final String connectionId;
  private final AmazonApiGatewayManagementApi api;
  private final String outputBucketName;
  private final AmazonS3 s3Client;
  private final String javabuilderSessionId;
  private int writes;

  // Temporary limit on writes per session until we find better way to limit usage.
  private static final int WRITES_PER_SESSION = 2;

  public AWSOutputAdapter(
      String connectionId,
      AmazonApiGatewayManagementApi api,
      String outputBucketName,
      String javabuilderSessionId) {
    this.connectionId = connectionId;
    this.api = api;
    this.outputBucketName = outputBucketName;
    this.s3Client = AmazonS3ClientBuilder.standard().build();
    this.javabuilderSessionId = javabuilderSessionId;
    this.writes = 0;
  }

  /**
   * POSTs a message to the API Gateway @connections url for the current user
   *
   * @param message The message to send to API Gateway from the user's program.
   */
  @Override
  public void sendMessage(ClientMessage message) {
    PostToConnectionRequest post = new PostToConnectionRequest();
    post.setConnectionId(connectionId);
    post.setData(ByteBuffer.wrap((message.getFormattedMessage()).getBytes()));
    api.postToConnection(post);
  }

  public void sendDebuggingMessage(ClientMessage message) {
    String time = String.valueOf(java.time.Clock.systemUTC().instant());
    PostToConnectionRequest post = new PostToConnectionRequest();
    post.setConnectionId(connectionId);
    post.setData(ByteBuffer.wrap((message + " " + time).getBytes()));
    api.postToConnection(post);
  }

  @Override
  public String writeToFile(String filename, InputStream input) throws JavabuilderException {
    if (this.writes >= WRITES_PER_SESSION) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.TOO_MANY_WRITES);
    }
    String filePath = this.javabuilderSessionId + "/" + filename;
    this.s3Client.putObject(this.outputBucketName, filePath, input, null);
    this.writes++;
    return "";
  }
}
