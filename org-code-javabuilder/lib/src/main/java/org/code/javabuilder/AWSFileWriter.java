package org.code.javabuilder;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.ByteArrayInputStream;
import org.code.protocol.*;

public class AWSFileWriter implements JavabuilderFileWriter {
  private final String outputBucketName;
  private final AmazonS3 s3Client;
  private final String javabuilderSessionId;
  private final String getOutputURL;
  private int writes;

  // Temporary limit on writes per session until we can more fully limit usage.
  private static final int WRITES_PER_SESSION = 2;

  public AWSFileWriter(
      AmazonS3 s3Client,
      String outputBucketName,
      String javabuilderSessionId,
      String getOutputURL) {
    this.outputBucketName = outputBucketName;
    this.s3Client = s3Client;
    this.javabuilderSessionId = javabuilderSessionId;
    this.writes = 0;
    this.getOutputURL = getOutputURL;
  }

  @Override
  public String writeToFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException {
    if (this.writes >= WRITES_PER_SESSION) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.TOO_MANY_WRITES);
    }
    String filePath = this.javabuilderSessionId + "/" + filename;
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(contentType);
    metadata.setContentLength(inputBytes.length);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBytes);

    try {
      this.s3Client.putObject(this.outputBucketName, filePath, inputStream, metadata);
    } catch (SdkClientException e) {
      // We couldn't write to S3, send a message to the user and fail. The S3 SDK includes retries.
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }

    this.writes++;
    return this.getOutputURL + "/" + filePath;
  }
}
